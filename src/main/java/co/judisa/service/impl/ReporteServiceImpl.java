package co.judisa.service.impl;

import co.judisa.dto.reporte.ArchivoReporte;
import co.judisa.dto.reporte.FormatoReporte;
import co.judisa.dto.reporte.ReporteResumenResponse;
import co.judisa.dto.reporte.ResumenEmpleadoReporte;
import co.judisa.dto.reporte.ResumenReferenciaReporte;
import co.judisa.exception.FieldValidationException;
import co.judisa.exception.ResourceNotFoundException;
import co.judisa.model.enums.EstadoPago;
import co.judisa.report.ExcelReportGenerator;
import co.judisa.report.PdfReportGenerator;
import co.judisa.repository.CorteRepository;
import co.judisa.repository.EmpleadoRepository;
import co.judisa.repository.PagoRepository;
import co.judisa.service.ReporteService;
import co.judisa.util.Formatos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {

    /** Límite para que un reporte no recorra décadas de datos por error. */
    private static final long MAXIMO_DIAS = 3660;

    private final CorteRepository corteRepository;
    private final PagoRepository pagoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final PdfReportGenerator pdf;
    private final ExcelReportGenerator excel;

    public ReporteServiceImpl(CorteRepository corteRepository, PagoRepository pagoRepository,
                              EmpleadoRepository empleadoRepository, PdfReportGenerator pdf, ExcelReportGenerator excel) {
        this.corteRepository = corteRepository;
        this.pagoRepository = pagoRepository;
        this.empleadoRepository = empleadoRepository;
        this.pdf = pdf;
        this.excel = excel;
    }

    /** RF-44 producción por periodo, RF-45 piezas por empleado y referencia, RF-47 pagos por periodo. */
    @Override
    public ReporteResumenResponse resumen(LocalDate desde, LocalDate hasta, Long empleadoId) {
        LocalDate hoy = Formatos.hoy();
        // Sin fechas: mes en curso. Solo "desde": hasta hoy. Solo "hasta": desde el inicio de ese mes.
        LocalDate d = desde != null ? desde : (hasta != null ? hasta.withDayOfMonth(1) : hoy.withDayOfMonth(1));
        LocalDate h = hasta != null ? hasta
                : (desde != null ? (desde.isAfter(hoy) ? desde : hoy) : hoy.withDayOfMonth(hoy.lengthOfMonth()));
        if (d.isAfter(h)) {
            throw new FieldValidationException("hasta", "La fecha final no puede ser anterior a la inicial");
        }
        if (ChronoUnit.DAYS.between(d, h) > MAXIMO_DIAS) {
            throw new FieldValidationException("desde", "El periodo del reporte no puede superar 10 años");
        }
        String nombre = null;
        if (empleadoId != null) {
            nombre = empleadoRepository.findById(empleadoId)
                    .orElseThrow(() -> ResourceNotFoundException.de("El empleado", empleadoId)).getNombre();
        }

        List<ResumenReferenciaReporte> porReferencia = new ArrayList<>();
        Map<Long, Acumulado> porEmpleado = new LinkedHashMap<>();
        for (Object[] fila : corteRepository.piezasPorEmpleadoYReferencia(empleadoId, d, h)) {
            Long id = (Long) fila[0];
            String empleado = (String) fila[1];
            long piezas = ((Number) fila[4]).longValue();
            BigDecimal valor = aDecimal(fila[5]);
            porReferencia.add(new ResumenReferenciaReporte(id, empleado, (String) fila[2], (String) fila[3], piezas, valor));
            Acumulado a = porEmpleado.computeIfAbsent(id, k -> new Acumulado(empleado));
            a.piezas += piezas;
            a.producido = a.producido.add(valor);
        }
        for (Object[] fila : pagoRepository.totalesPorEmpleadoYEstado(empleadoId, d, h)) {
            Acumulado a = porEmpleado.computeIfAbsent((Long) fila[0], k -> new Acumulado((String) fila[1]));
            BigDecimal total = aDecimal(fila[3]);
            if (fila[2] == EstadoPago.REALIZADO) {
                a.pagado = a.pagado.add(total);
            } else {
                a.pendiente = a.pendiente.add(total);
            }
        }

        List<ResumenEmpleadoReporte> filas = porEmpleado.entrySet().stream()
                .map(e -> new ResumenEmpleadoReporte(e.getKey(), e.getValue().nombre, e.getValue().piezas,
                        e.getValue().producido, e.getValue().pagado, e.getValue().pendiente))
                .sorted(Comparator.comparing(ResumenEmpleadoReporte::empleado, String.CASE_INSENSITIVE_ORDER))
                .toList();
        return new ReporteResumenResponse(d, h, empleadoId, nombre,
                filas.stream().mapToLong(ResumenEmpleadoReporte::piezas).sum(),
                filas.stream().map(ResumenEmpleadoReporte::valorProducido).reduce(BigDecimal.ZERO, BigDecimal::add),
                filas.stream().map(ResumenEmpleadoReporte::totalPagado).reduce(BigDecimal.ZERO, BigDecimal::add),
                filas.stream().map(ResumenEmpleadoReporte::totalPendiente).reduce(BigDecimal.ZERO, BigDecimal::add),
                filas, List.copyOf(porReferencia));
    }

    /** RF-46: el mismo reporte como PDF o Excel. */
    @Override
    public ArchivoReporte generar(LocalDate desde, LocalDate hasta, Long empleadoId, FormatoReporte formato) {
        ReporteResumenResponse reporte = resumen(desde, hasta, empleadoId);
        byte[] contenido = formato == FormatoReporte.XLSX ? excel.generar(reporte) : pdf.generar(reporte);
        String nombre = "reporte-judisa-" + reporte.desde() + "_" + reporte.hasta() + "." + formato.getExtension();
        return new ArchivoReporte(nombre, formato.getContentType(), contenido);
    }

    private static BigDecimal aDecimal(Object valor) {
        if (valor == null) {
            return BigDecimal.ZERO;
        }
        return valor instanceof BigDecimal b ? b : new BigDecimal(valor.toString());
    }

    private static final class Acumulado {
        private final String nombre;
        private long piezas;
        private BigDecimal producido = BigDecimal.ZERO;
        private BigDecimal pagado = BigDecimal.ZERO;
        private BigDecimal pendiente = BigDecimal.ZERO;

        private Acumulado(String nombre) {
            this.nombre = nombre;
        }
    }
}

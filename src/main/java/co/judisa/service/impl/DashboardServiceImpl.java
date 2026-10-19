package co.judisa.service.impl;

import co.judisa.dto.corte.TotalesCortes;
import co.judisa.dto.dashboard.DashboardResponse;
import co.judisa.dto.dashboard.DashboardResponse.PagoEmpleado;
import co.judisa.dto.dashboard.DashboardResponse.PiezasSemana;
import co.judisa.dto.pago.TotalesPagos;
import co.judisa.model.enums.EstadoPago;
import co.judisa.repository.CorteRepository;
import co.judisa.repository.EmpleadoRepository;
import co.judisa.repository.PagoRepository;
import co.judisa.repository.ReferenciaRepository;
import co.judisa.service.DashboardService;
import co.judisa.util.Formatos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final String[] MESES = {"ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic"};

    private final EmpleadoRepository empleadoRepository;
    private final ReferenciaRepository referenciaRepository;
    private final CorteRepository corteRepository;
    private final PagoRepository pagoRepository;

    public DashboardServiceImpl(EmpleadoRepository empleadoRepository, ReferenciaRepository referenciaRepository,
                                CorteRepository corteRepository, PagoRepository pagoRepository) {
        this.empleadoRepository = empleadoRepository;
        this.referenciaRepository = referenciaRepository;
        this.corteRepository = corteRepository;
        this.pagoRepository = pagoRepository;
    }

    /** RF-50 conteos, RF-51 piezas y pagos del mes en curso, RF-52 alerta de pagos pendientes. */
    @Override
    public DashboardResponse resumen() {
        LocalDate hoy = Formatos.hoy();
        LocalDate inicio = hoy.withDayOfMonth(1);
        LocalDate fin = hoy.withDayOfMonth(hoy.lengthOfMonth());
        TotalesCortes mes = corteRepository.totalizar(null, inicio, fin);
        TotalesPagos pagosMes = pagoRepository.totalizar(null, inicio, fin);
        return new DashboardResponse(
                empleadoRepository.countByActivoTrue(),
                referenciaRepository.countByActivaTrue(),
                corteRepository.count(),
                pagoRepository.count(),
                mes.piezas(),
                mes.cortes(),
                pagosMes.totalPagado(),
                pagoRepository.countByEstado(EstadoPago.PENDIENTE),
                pagoRepository.sumarPorEstado(EstadoPago.PENDIENTE),
                piezasPorSemana(inicio, fin),
                pagosPorEmpleado(inicio, fin));
    }

    /** Gráfico de producción: semanas fijas del mes (1–7, 8–14, 15–21, 22–28, 29–fin). */
    private List<PiezasSemana> piezasPorSemana(LocalDate inicio, LocalDate fin) {
        long[] porSemana = new long[5];
        for (Object[] fila : corteRepository.piezasPorDia(inicio, fin)) {
            int dia = ((LocalDate) fila[0]).getDayOfMonth();
            porSemana[Math.min((dia - 1) / 7, 4)] += ((Number) fila[1]).longValue();
        }
        String mes = MESES[inicio.getMonthValue() - 1];
        List<PiezasSemana> semanas = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int desde = i * 7 + 1;
            if (desde > fin.getDayOfMonth()) {
                break;
            }
            int hasta = i == 4 ? fin.getDayOfMonth() : desde + 6;
            String etiqueta = desde == hasta ? desde + " " + mes : desde + "–" + hasta + " " + mes;
            semanas.add(new PiezasSemana(etiqueta, porSemana[i]));
        }
        return semanas;
    }

    /** Gráfico de pagos: pagos REALIZADOS del mes por empleado, de mayor a menor. */
    private List<PagoEmpleado> pagosPorEmpleado(LocalDate inicio, LocalDate fin) {
        return pagoRepository.totalesPorEmpleadoYEstado(null, inicio, fin).stream()
                .filter(fila -> fila[2] == EstadoPago.REALIZADO)
                .map(fila -> new PagoEmpleado((Long) fila[0], (String) fila[1],
                        fila[3] instanceof BigDecimal b ? b : new BigDecimal(fila[3].toString())))
                .sorted(Comparator.comparing(PagoEmpleado::total).reversed())
                .toList();
    }
}

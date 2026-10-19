package co.judisa.service.impl;

import co.judisa.dto.comun.PageResponse;
import co.judisa.dto.corte.CorteFiltro;
import co.judisa.dto.historial.HistorialFiltro;
import co.judisa.dto.historial.MovimientoHistorialDTO;
import co.judisa.dto.pago.PagoFiltro;
import co.judisa.exception.FieldValidationException;
import co.judisa.model.entity.Corte;
import co.judisa.model.entity.Pago;
import co.judisa.model.enums.TipoMovimiento;
import co.judisa.repository.CorteRepository;
import co.judisa.repository.PagoRepository;
import co.judisa.service.HistorialService;
import co.judisa.util.Paginacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Une cortes y pagos en una sola línea de tiempo. Cada tabla se consulta ya ordenada y limitada a lo que puede
 * aparecer hasta la página pedida; luego se mezclan en memoria. Así no hace falta un UNION nativo distinto para
 * PostgreSQL y MySQL.
 */
@Service
@Transactional(readOnly = true)
public class HistorialServiceImpl implements HistorialService {

    private static final String PAGO_SIN_NOTA = "Pago de producción";

    /** RF-41: orden cronológico descendente; a igual fecha, lo registrado más reciente primero. */
    static final Comparator<MovimientoHistorialDTO> ORDEN = Comparator
            .comparing(MovimientoHistorialDTO::fecha, Comparator.reverseOrder())
            .thenComparing(MovimientoHistorialDTO::registradoEn, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(MovimientoHistorialDTO::id, Comparator.reverseOrder());

    private static final Sort ORDEN_SQL = Sort.by(Sort.Direction.DESC, "fecha", "createdAt", "id");

    private final CorteRepository corteRepository;
    private final PagoRepository pagoRepository;

    public HistorialServiceImpl(CorteRepository corteRepository, PagoRepository pagoRepository) {
        this.corteRepository = corteRepository;
        this.pagoRepository = pagoRepository;
    }

    /** RF-40 historial, RF-42 filtro por empleado, RF-43 filtro por fechas; filtro opcional por tipo. */
    @Override
    public PageResponse<MovimientoHistorialDTO> listar(HistorialFiltro filtro, int page, int size) {
        if (filtro.desde() != null && filtro.hasta() != null && filtro.desde().isAfter(filtro.hasta())) {
            throw new FieldValidationException("hasta", "La fecha final no puede ser anterior a la inicial");
        }
        int pagina = Math.max(page, 0);
        int tamano = size <= 0 ? Paginacion.TAMANO_POR_DEFECTO : Math.min(size, Paginacion.TAMANO_MAXIMO);
        int limite = (pagina + 1) * tamano;
        PageRequest ventana = PageRequest.of(0, limite, ORDEN_SQL);

        List<MovimientoHistorialDTO> mezcla = new ArrayList<>();
        long total = 0;
        if (filtro.tipo() == null || filtro.tipo() == TipoMovimiento.CORTE) {
            Page<Corte> cortes = corteRepository.findAll(CorteRepository.conFiltro(
                    new CorteFiltro(filtro.empleadoId(), null, filtro.desde(), filtro.hasta())), ventana);
            cortes.forEach(c -> mezcla.add(desdeCorte(c)));
            total += cortes.getTotalElements();
        }
        if (filtro.tipo() == null || filtro.tipo() == TipoMovimiento.PAGO) {
            Page<Pago> pagos = pagoRepository.findAll(PagoRepository.conFiltro(
                    new PagoFiltro(filtro.empleadoId(), null, filtro.desde(), filtro.hasta())), ventana);
            pagos.forEach(p -> mezcla.add(desdePago(p)));
            total += pagos.getTotalElements();
        }
        mezcla.sort(ORDEN);
        int desde = Math.min(pagina * tamano, mezcla.size());
        int hasta = Math.min(desde + tamano, mezcla.size());
        int paginas = (int) Math.ceil((double) total / tamano);
        return new PageResponse<>(List.copyOf(mezcla.subList(desde, hasta)), pagina, tamano, total, paginas);
    }

    static MovimientoHistorialDTO desdeCorte(Corte c) {
        BigDecimal valor = c.getReferencia().getValorUnitario().multiply(BigDecimal.valueOf(c.getCantidad()));
        String detalle = c.getReferencia().getCodigo() + " · " + c.getReferencia().getDescripcion();
        return new MovimientoHistorialDTO(TipoMovimiento.CORTE, c.getId(), c.getFecha(), c.getEmpleado().getId(),
                c.getEmpleado().getNombre(), detalle, c.getCantidad(), valor, null, c.getCreatedAt());
    }

    static MovimientoHistorialDTO desdePago(Pago p) {
        String detalle = p.getNota() == null || p.getNota().isBlank() ? PAGO_SIN_NOTA : p.getNota();
        return new MovimientoHistorialDTO(TipoMovimiento.PAGO, p.getId(), p.getFecha(), p.getEmpleado().getId(),
                p.getEmpleado().getNombre(), detalle, p.getPiezas(), p.getTotal(), p.getEstado(), p.getCreatedAt());
    }
}

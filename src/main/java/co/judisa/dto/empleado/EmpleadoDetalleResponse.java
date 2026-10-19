package co.judisa.dto.empleado;

import co.judisa.dto.corte.CorteResponse;

import java.math.BigDecimal;
import java.util.List;

/** RF-17: detalle de un empleado con su historial de cortes y pagos. */
public record EmpleadoDetalleResponse(
        EmpleadoResponse empleado,
        long totalCortes,
        long totalPiezas,
        long totalPagos,
        BigDecimal totalPagado,
        List<CorteResponse> cortes,
        List<PagoResumenResponse> pagos) {
}

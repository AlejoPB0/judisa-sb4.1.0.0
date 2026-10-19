package co.judisa.dto.pago;

import java.math.BigDecimal;
import java.time.LocalDate;

/** RF-37: total pagado (y pendiente) a un empleado en un periodo. */
public record TotalPagosResponse(
        Long empleadoId,
        String empleadoNombre,
        LocalDate desde,
        LocalDate hasta,
        BigDecimal totalPagado,
        long piezasPagadas,
        BigDecimal totalPendiente,
        long piezasPendientes,
        long cantidadPagos) {
}

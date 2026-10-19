package co.judisa.dto.pago;

import java.math.BigDecimal;
import java.time.LocalDate;

/** RF-39: piezas cortadas frente a piezas pagadas, y su equivalente en dinero. */
public record SaldoPendienteResponse(
        Long empleadoId,
        String empleadoNombre,
        LocalDate desde,
        LocalDate hasta,
        long piezasCortadas,
        BigDecimal valorProducido,
        long piezasPagadas,
        BigDecimal totalPagado,
        long piezasEnPagosPendientes,
        BigDecimal totalEnPagosPendientes,
        long piezasSaldo,
        BigDecimal saldoPendiente) {
}

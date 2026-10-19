package co.judisa.dto.pago;

import java.math.BigDecimal;

/** Proyección de la consulta agregada de pagos por estado. */
public record TotalesPagos(BigDecimal totalPagado, Long piezasPagadas, BigDecimal totalPendiente,
                           Long piezasPendientes, Long cantidad) {
}

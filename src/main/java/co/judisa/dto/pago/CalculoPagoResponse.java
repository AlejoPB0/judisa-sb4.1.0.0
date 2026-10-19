package co.judisa.dto.pago;

import java.math.BigDecimal;

public record CalculoPagoResponse(Integer piezas, BigDecimal valorUnitario, BigDecimal total) {
}

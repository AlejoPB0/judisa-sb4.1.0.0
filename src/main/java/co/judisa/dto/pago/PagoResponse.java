package co.judisa.dto.pago;

import co.judisa.model.enums.EstadoPago;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record PagoResponse(
        Long id,
        Long empleadoId,
        String empleadoNombre,
        Integer piezas,
        BigDecimal valorUnitario,
        BigDecimal total,
        String nota,
        LocalDate fecha,
        EstadoPago estado,
        String estadoNombre,
        LocalDate fechaPagoEfectivo,
        String clientId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}

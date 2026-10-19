package co.judisa.dto.pago;

import co.judisa.model.enums.EstadoPago;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

/** RF-38: marcar un pago como pendiente o realizado. */
public record CambioEstadoPagoRequest(
        @NotNull(message = "Indique el estado") EstadoPago estado,
        @PastOrPresent(message = "La fecha de pago no puede ser futura") LocalDate fechaPagoEfectivo) {
}

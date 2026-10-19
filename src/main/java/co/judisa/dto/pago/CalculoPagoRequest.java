package co.judisa.dto.pago;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/** RF-36: datos para la vista previa del total. */
public record CalculoPagoRequest(
        @NotNull(message = "Las piezas son obligatorias")
        @Positive(message = "Las piezas deben ser mayores que cero")
        @Max(value = 1_000_000, message = "Las piezas no pueden superar 1.000.000") Integer piezas,
        @NotNull(message = "El valor unitario es obligatorio")
        @Positive(message = "El valor unitario debe ser mayor que cero")
        @Digits(integer = 12, fraction = 0, message = "El valor unitario debe ser un número entero de pesos") BigDecimal valorUnitario) {
}

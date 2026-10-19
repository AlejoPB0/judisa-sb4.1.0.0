package co.judisa.dto.historial;

import co.judisa.model.enums.EstadoPago;
import co.judisa.model.enums.TipoMovimiento;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * RF-40, RF-41: un corte o un pago en el historial unificado. Para cortes, {@code valor} es el valor de producción
 * estimado (cantidad × valor unitario actual de la referencia); para pagos, el total del pago.
 */
public record MovimientoHistorialDTO(
        TipoMovimiento tipo,
        Long id,
        LocalDate fecha,
        Long empleadoId,
        String empleado,
        String detalle,
        Integer cantidad,
        BigDecimal valor,
        EstadoPago estado,
        @JsonIgnore OffsetDateTime registradoEn) {
}

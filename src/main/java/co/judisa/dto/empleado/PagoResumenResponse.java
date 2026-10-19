package co.judisa.dto.empleado;

import co.judisa.model.enums.EstadoPago;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Vista de solo lectura de un pago dentro del detalle del empleado. */
public record PagoResumenResponse(
        Long id,
        LocalDate fecha,
        Integer piezas,
        BigDecimal valorUnitario,
        BigDecimal total,
        EstadoPago estado,
        String nota) {
}

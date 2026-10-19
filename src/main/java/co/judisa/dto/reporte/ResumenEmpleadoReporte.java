package co.judisa.dto.reporte;

import java.math.BigDecimal;

/** RF-44, RF-47: producción y pagos de un empleado en el periodo. */
public record ResumenEmpleadoReporte(Long empleadoId, String empleado, long piezas, BigDecimal valorProducido,
                                     BigDecimal totalPagado, BigDecimal totalPendiente) {
}

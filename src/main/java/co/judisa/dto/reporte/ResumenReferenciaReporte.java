package co.judisa.dto.reporte;

import java.math.BigDecimal;

/** RF-45: piezas por empleado y referencia en el periodo. */
public record ResumenReferenciaReporte(Long empleadoId, String empleado, String referenciaCodigo,
                                       String referenciaDescripcion, long piezas, BigDecimal valorProducido) {
}

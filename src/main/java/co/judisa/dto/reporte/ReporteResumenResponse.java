package co.judisa.dto.reporte;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** RF-44 a RF-47: reporte de producción y pagos de un periodo. */
public record ReporteResumenResponse(
        LocalDate desde,
        LocalDate hasta,
        Long empleadoId,
        String empleadoNombre,
        long totalPiezas,
        BigDecimal valorProducido,
        BigDecimal totalPagado,
        BigDecimal totalPendiente,
        List<ResumenEmpleadoReporte> porEmpleado,
        List<ResumenReferenciaReporte> porEmpleadoReferencia) {
}

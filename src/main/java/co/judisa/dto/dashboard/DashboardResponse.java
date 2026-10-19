package co.judisa.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

/** RF-48 a RF-52: resumen del panel principal. */
public record DashboardResponse(
        long totalEmpleadosActivos,
        long totalReferenciasActivas,
        long totalCortes,
        long totalPagos,
        long piezasMesActual,
        long cortesMesActual,
        BigDecimal pagadoMesActual,
        long pagosPendientesCantidad,
        BigDecimal pagosPendientesMonto,
        List<PiezasSemana> piezasPorSemana,
        List<PagoEmpleado> pagosPorEmpleado) {

    /** Piezas cortadas en una semana del mes (días 1–7, 8–14, 15–21, 22–28, 29–fin). */
    public record PiezasSemana(String etiqueta, long piezas) {
    }

    /** Pagos realizados en el mes a un empleado. */
    public record PagoEmpleado(Long empleadoId, String empleado, BigDecimal total) {
    }
}

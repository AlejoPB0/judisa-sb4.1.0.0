package co.judisa.dto.historial;

import co.judisa.model.enums.TipoMovimiento;

import java.time.LocalDate;

/** RF-42, RF-43: filtros del historial por empleado, rango de fechas y tipo. */
public record HistorialFiltro(Long empleadoId, LocalDate desde, LocalDate hasta, TipoMovimiento tipo) {
}

package co.judisa.dto.corte;

import java.time.LocalDate;

/** RF-27: total de piezas cortadas (por empleado y/o periodo). */
public record TotalPiezasResponse(
        Long empleadoId,
        String empleadoNombre,
        LocalDate desde,
        LocalDate hasta,
        long totalPiezas,
        long totalCortes) {
}

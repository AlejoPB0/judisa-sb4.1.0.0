package co.judisa.dto.corte;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record CorteResponse(
        Long id,
        Long referenciaId,
        String referenciaCodigo,
        String referenciaDescripcion,
        Long empleadoId,
        String empleadoNombre,
        Integer cantidad,
        LocalDate fecha,
        String observaciones,
        String clientId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}

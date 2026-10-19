package co.judisa.dto.empleado;

import co.judisa.model.enums.TipoTrabajo;

import java.time.OffsetDateTime;

public record EmpleadoResponse(
        Long id,
        String nombre,
        String documento,
        String telefono,
        TipoTrabajo tipoTrabajo,
        String tipoTrabajoNombre,
        boolean activo,
        String clientId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}

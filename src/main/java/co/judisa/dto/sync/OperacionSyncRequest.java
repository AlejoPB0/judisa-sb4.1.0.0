package co.judisa.dto.sync;

import co.judisa.model.enums.EntidadSync;
import co.judisa.model.enums.OperacionSync;
import tools.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

/**
 * RF-57: una operación hecha sin conexión en el teléfono.
 *
 * @param clientId     UUID generado por la app, uno por operación (clave de idempotencia)
 * @param id           id del servidor del registro (ACTUALIZAR / ELIMINAR)
 * @param idClientId   alternativa a {@code id}: clientId de la operación CREAR que originó el registro
 * @param modificadoEn momento del cambio en el teléfono (gana el último cambio)
 * @param datos        cuerpo de EmpleadoRequest, ReferenciaRequest, CorteRequest o PagoRequest
 */
public record OperacionSyncRequest(
        @NotBlank(message = "Cada operación necesita clientId")
        @Size(max = 36, message = "clientId admite máximo 36 caracteres") String clientId,
        @NotNull(message = "Indique la entidad") EntidadSync entidad,
        @NotNull(message = "Indique la operación") OperacionSync operacion,
        Long id,
        @Size(max = 36) String idClientId,
        OffsetDateTime modificadoEn,
        JsonNode datos) {
}

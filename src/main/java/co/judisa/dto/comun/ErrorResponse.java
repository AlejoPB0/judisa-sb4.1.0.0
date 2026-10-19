package co.judisa.dto.comun;

import java.time.OffsetDateTime;
import java.util.Map;

/** Formato uniforme de error de la API. {@code fields} solo trae datos en errores de validación. */
public record ErrorResponse(OffsetDateTime timestamp, int status, String error, String message, Map<String, String> fields) {
}

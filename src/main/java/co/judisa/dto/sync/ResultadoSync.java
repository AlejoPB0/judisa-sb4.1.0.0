package co.judisa.dto.sync;

import co.judisa.model.enums.EstadoSync;

/** Resultado de una operación; {@code registro} es la versión del servidor (o null si no existe). */
public record ResultadoSync(String clientId, EstadoSync estado, Long id, String mensaje, Object registro) {
}

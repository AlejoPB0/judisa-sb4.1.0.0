package co.judisa.model.enums;

/** Resultado de una operación enviada por POST /api/v1/sync (RF-57). */
public enum EstadoSync {
    /** Se aplicó en el servidor. */
    APLICADA,
    /** El mismo clientId ya se había aplicado (reintento); no se repite. */
    YA_PROCESADA,
    /** El servidor tiene un cambio más reciente (gana el último cambio); no se aplicó. */
    CONFLICTO,
    /** Error de validación o de regla de negocio. */
    RECHAZADA
}

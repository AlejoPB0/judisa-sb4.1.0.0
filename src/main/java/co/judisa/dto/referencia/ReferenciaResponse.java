package co.judisa.dto.referencia;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ReferenciaResponse(
        Long id,
        String codigo,
        String descripcion,
        BigDecimal valorUnitario,
        boolean activa,
        String clientId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}

package co.judisa.dto.sync;

import java.time.OffsetDateTime;
import java.util.List;

public record SyncResponse(OffsetDateTime servidorEn, List<ResultadoSync> resultados) {
}

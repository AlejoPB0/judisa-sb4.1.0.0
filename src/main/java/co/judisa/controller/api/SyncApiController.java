package co.judisa.controller.api;

import co.judisa.dto.sync.SyncRequest;
import co.judisa.dto.sync.SyncResponse;
import co.judisa.service.SyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sync")
@Tag(name = "Sincronización", description = "RF-57")
public class SyncApiController {

    private final SyncService syncService;

    public SyncApiController(SyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping
    @Operation(summary = "Sincronizar la cola de operaciones hechas sin conexión (RF-57)",
            description = "Idempotente por clientId; gana el último cambio; máximo 500 operaciones")
    public SyncResponse sincronizar(@Valid @RequestBody SyncRequest request) {
        return syncService.sincronizar(request);
    }
}

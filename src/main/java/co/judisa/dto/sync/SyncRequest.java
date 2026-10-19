package co.judisa.dto.sync;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** RF-57: cola de operaciones pendientes, en el orden en que se hicieron. */
public record SyncRequest(
        @NotNull(message = "Envíe la lista de operaciones")
        @Size(max = 500, message = "Máximo 500 operaciones por petición")
        List<@Valid @NotNull OperacionSyncRequest> operaciones) {
}

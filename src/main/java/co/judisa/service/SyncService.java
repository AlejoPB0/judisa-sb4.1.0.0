package co.judisa.service;

import co.judisa.dto.sync.SyncRequest;
import co.judisa.dto.sync.SyncResponse;

/** RF-57: aplica en el servidor las operaciones que las apps hicieron sin conexión. */
public interface SyncService {

    SyncResponse sincronizar(SyncRequest request);
}

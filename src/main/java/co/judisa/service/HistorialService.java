package co.judisa.service;

import co.judisa.dto.comun.PageResponse;
import co.judisa.dto.historial.HistorialFiltro;
import co.judisa.dto.historial.MovimientoHistorialDTO;

/** Módulo 5: historial de producción y pagos (RF-40 a RF-43). */
public interface HistorialService {

    PageResponse<MovimientoHistorialDTO> listar(HistorialFiltro filtro, int page, int size);
}

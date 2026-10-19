package co.judisa.service;

import co.judisa.dto.comun.PageResponse;
import co.judisa.dto.corte.CorteFiltro;
import co.judisa.dto.corte.CorteRequest;
import co.judisa.dto.corte.CorteResponse;
import co.judisa.dto.corte.TotalPiezasResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/** Módulo 3: cortes de producción (RF-18). */
public interface CorteService {

    PageResponse<CorteResponse> listar(CorteFiltro filtro, Pageable pageable);

    CorteResponse obtener(Long id);

    CorteRequest obtenerParaEditar(Long id);

    CorteResponse crear(CorteRequest request);

    CorteResponse actualizar(Long id, CorteRequest request);

    void eliminar(Long id);

    TotalPiezasResponse totalPiezas(Long empleadoId, LocalDate desde, LocalDate hasta);
}

package co.judisa.service;

import co.judisa.dto.comun.PageResponse;
import co.judisa.dto.referencia.ReferenciaRequest;
import co.judisa.dto.referencia.ReferenciaResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/** CRUD de referencias (soporte del módulo de cortes). */
public interface ReferenciaService {

    PageResponse<ReferenciaResponse> listar(String q, Boolean activa, Pageable pageable);

    List<ReferenciaResponse> listarActivas();

    ReferenciaResponse obtener(Long id);

    ReferenciaRequest obtenerParaEditar(Long id);

    ReferenciaResponse crear(ReferenciaRequest request);

    ReferenciaResponse actualizar(Long id, ReferenciaRequest request);

    void eliminar(Long id);

    ReferenciaResponse cambiarEstado(Long id, boolean activa);
}

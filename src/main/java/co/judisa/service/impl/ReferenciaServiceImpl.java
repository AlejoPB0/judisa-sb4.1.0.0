package co.judisa.service.impl;

import co.judisa.dto.comun.PageResponse;
import co.judisa.dto.referencia.ReferenciaRequest;
import co.judisa.dto.referencia.ReferenciaResponse;
import co.judisa.exception.BusinessRuleException;
import co.judisa.exception.ResourceNotFoundException;
import co.judisa.mapper.ReferenciaMapper;
import co.judisa.model.entity.Referencia;
import co.judisa.repository.CorteRepository;
import co.judisa.repository.ReferenciaRepository;
import co.judisa.service.ReferenciaService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReferenciaServiceImpl implements ReferenciaService {

    private final ReferenciaRepository referenciaRepository;
    private final CorteRepository corteRepository;
    private final ReferenciaMapper mapper;

    public ReferenciaServiceImpl(ReferenciaRepository referenciaRepository, CorteRepository corteRepository,
                                 ReferenciaMapper mapper) {
        this.referenciaRepository = referenciaRepository;
        this.corteRepository = corteRepository;
        this.mapper = mapper;
    }

    @Override
    public PageResponse<ReferenciaResponse> listar(String q, Boolean activa, Pageable pageable) {
        Specification<Referencia> spec = Specification.allOf(
                ReferenciaRepository.busqueda(q), ReferenciaRepository.conEstado(activa));
        return PageResponse.of(referenciaRepository.findAll(spec, pageable), mapper::toResponse);
    }

    @Override
    public List<ReferenciaResponse> listarActivas() {
        return referenciaRepository.findByActivaTrueOrderByCodigoAsc().stream().map(mapper::toResponse).toList();
    }

    @Override
    public ReferenciaResponse obtener(Long id) {
        return mapper.toResponse(buscar(id));
    }

    @Override
    public ReferenciaRequest obtenerParaEditar(Long id) {
        return mapper.toRequest(buscar(id));
    }

    @Override
    @Transactional
    public ReferenciaResponse crear(ReferenciaRequest request) {
        String codigo = ReferenciaMapper.normalizarCodigo(request.getCodigo());
        if (referenciaRepository.existsByCodigo(codigo)) {
            throw codigoDuplicado(codigo);
        }
        Referencia referencia = new Referencia();
        mapper.actualizar(referencia, request);
        return mapper.toResponse(referenciaRepository.saveAndFlush(referencia));
    }

    @Override
    @Transactional
    public ReferenciaResponse actualizar(Long id, ReferenciaRequest request) {
        Referencia referencia = buscar(id);
        String codigo = ReferenciaMapper.normalizarCodigo(request.getCodigo());
        if (referenciaRepository.existsByCodigoAndIdNot(codigo, id)) {
            throw codigoDuplicado(codigo);
        }
        mapper.actualizar(referencia, request);
        return mapper.toResponse(referenciaRepository.saveAndFlush(referencia));
    }

    /** Igual que con empleados: no se elimina una referencia usada en cortes; se puede desactivar. */
    @Override
    @Transactional
    public void eliminar(Long id) {
        Referencia referencia = buscar(id);
        long cortes = corteRepository.countByReferenciaId(id);
        if (cortes > 0) {
            throw new BusinessRuleException("La referencia " + referencia.getCodigo() + " tiene " + cortes
                    + (cortes == 1 ? " corte" : " cortes") + " registrados. No se puede eliminar; puede desactivarla.");
        }
        referenciaRepository.delete(referencia);
    }

    @Override
    @Transactional
    public ReferenciaResponse cambiarEstado(Long id, boolean activa) {
        Referencia referencia = buscar(id);
        referencia.setActiva(activa);
        return mapper.toResponse(referenciaRepository.saveAndFlush(referencia));
    }

    private Referencia buscar(Long id) {
        return referenciaRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.de("La referencia", id));
    }

    private BusinessRuleException codigoDuplicado(String codigo) {
        return new BusinessRuleException("Ya existe una referencia con el código " + codigo);
    }
}

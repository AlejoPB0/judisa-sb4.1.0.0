package co.judisa.service.impl;

import co.judisa.dto.comun.PageResponse;
import co.judisa.dto.corte.CorteFiltro;
import co.judisa.dto.corte.CorteRequest;
import co.judisa.dto.corte.CorteResponse;
import co.judisa.dto.corte.TotalPiezasResponse;
import co.judisa.dto.corte.TotalesCortes;
import co.judisa.exception.BusinessRuleException;
import co.judisa.exception.FieldValidationException;
import co.judisa.exception.ResourceNotFoundException;
import co.judisa.mapper.CorteMapper;
import co.judisa.model.entity.Corte;
import co.judisa.model.entity.Empleado;
import co.judisa.model.entity.Referencia;
import co.judisa.repository.CorteRepository;
import co.judisa.repository.EmpleadoRepository;
import co.judisa.repository.ReferenciaRepository;
import co.judisa.service.CorteService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class CorteServiceImpl implements CorteService {

    private static final LocalDate FECHA_MINIMA = LocalDate.of(1900, 1, 1);
    private static final LocalDate FECHA_MAXIMA = LocalDate.of(9999, 12, 31);

    private final CorteRepository corteRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ReferenciaRepository referenciaRepository;
    private final CorteMapper mapper;

    public CorteServiceImpl(CorteRepository corteRepository, EmpleadoRepository empleadoRepository,
                            ReferenciaRepository referenciaRepository, CorteMapper mapper) {
        this.corteRepository = corteRepository;
        this.empleadoRepository = empleadoRepository;
        this.referenciaRepository = referenciaRepository;
        this.mapper = mapper;
    }

    /** RF-22 filtros, RF-25 orden por fecha (lo define el Pageable), RF-26 búsqueda por código de referencia. */
    @Override
    public PageResponse<CorteResponse> listar(CorteFiltro filtro, Pageable pageable) {
        validarRango(filtro.desde(), filtro.hasta());
        return PageResponse.of(corteRepository.findAll(CorteRepository.conFiltro(filtro), pageable), mapper::toResponse);
    }

    @Override
    public CorteResponse obtener(Long id) {
        return mapper.toResponse(buscar(id));
    }

    @Override
    public CorteRequest obtenerParaEditar(Long id) {
        return mapper.toRequest(buscar(id));
    }

    /** RF-19 registrar; RF-23 cantidad positiva (DTO); RF-24 empleado existente y activo; RF-28 observaciones. */
    @Override
    @Transactional
    public CorteResponse crear(CorteRequest request) {
        Corte corte = new Corte();
        corte.setEmpleado(empleadoAsignable(request.getEmpleadoId(), null));
        corte.setReferencia(referenciaAsignable(request.getReferenciaId(), null));
        copiarDatos(corte, request);
        return mapper.toResponse(corteRepository.saveAndFlush(corte));
    }

    /** RF-20 modificar. Si se conserva el mismo empleado o referencia, se permite aunque hoy estén inactivos. */
    @Override
    @Transactional
    public CorteResponse actualizar(Long id, CorteRequest request) {
        Corte corte = buscar(id);
        corte.setEmpleado(empleadoAsignable(request.getEmpleadoId(), corte.getEmpleado()));
        corte.setReferencia(referenciaAsignable(request.getReferenciaId(), corte.getReferencia()));
        copiarDatos(corte, request);
        return mapper.toResponse(corteRepository.saveAndFlush(corte));
    }

    /** RF-21 eliminar. */
    @Override
    @Transactional
    public void eliminar(Long id) {
        corteRepository.delete(buscar(id));
    }

    /** RF-27: total de piezas cortadas por empleado (opcional) en un periodo (opcional). */
    @Override
    public TotalPiezasResponse totalPiezas(Long empleadoId, LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        String nombre = null;
        if (empleadoId != null) {
            nombre = empleadoRepository.findById(empleadoId)
                    .orElseThrow(() -> ResourceNotFoundException.de("El empleado", empleadoId)).getNombre();
        }
        TotalesCortes totales = corteRepository.totalizar(empleadoId,
                Objects.requireNonNullElse(desde, FECHA_MINIMA), Objects.requireNonNullElse(hasta, FECHA_MAXIMA));
        return new TotalPiezasResponse(empleadoId, nombre, desde, hasta, totales.piezas(), totales.cortes());
    }

    private void copiarDatos(Corte corte, CorteRequest request) {
        corte.setCantidad(request.getCantidad());
        corte.setFecha(request.getFecha());
        String obs = request.getObservaciones();
        corte.setObservaciones(obs == null || obs.isBlank() ? null : obs.trim());
    }

    /** RF-24: el empleado debe existir y estar activo para asociarlo a un corte. */
    private Empleado empleadoAsignable(Long empleadoId, Empleado actual) {
        if (actual != null && actual.getId().equals(empleadoId)) {
            return actual;
        }
        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> ResourceNotFoundException.de("El empleado", empleadoId));
        if (!empleado.isActivo()) {
            throw new BusinessRuleException("El empleado " + empleado.getNombre()
                    + " está inactivo. Actívelo antes de asignarle cortes.");
        }
        return empleado;
    }

    private Referencia referenciaAsignable(Long referenciaId, Referencia actual) {
        if (actual != null && actual.getId().equals(referenciaId)) {
            return actual;
        }
        Referencia referencia = referenciaRepository.findById(referenciaId)
                .orElseThrow(() -> ResourceNotFoundException.de("La referencia", referenciaId));
        if (!referencia.isActiva()) {
            throw new BusinessRuleException("La referencia " + referencia.getCodigo() + " está inactiva.");
        }
        return referencia;
    }

    private static void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new FieldValidationException("hasta", "La fecha final no puede ser anterior a la inicial");
        }
    }

    private Corte buscar(Long id) {
        return corteRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.de("El corte", id));
    }
}

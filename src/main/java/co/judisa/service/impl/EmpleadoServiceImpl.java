package co.judisa.service.impl;

import co.judisa.dto.comun.PageResponse;
import co.judisa.dto.corte.TotalesCortes;
import co.judisa.dto.empleado.EmpleadoDetalleResponse;
import co.judisa.dto.empleado.EmpleadoRequest;
import co.judisa.dto.empleado.EmpleadoResponse;
import co.judisa.exception.BusinessRuleException;
import co.judisa.exception.ResourceNotFoundException;
import co.judisa.mapper.CorteMapper;
import co.judisa.mapper.EmpleadoMapper;
import co.judisa.model.entity.Empleado;
import co.judisa.model.enums.TipoTrabajo;
import co.judisa.repository.CorteRepository;
import co.judisa.repository.EmpleadoRepository;
import co.judisa.repository.PagoRepository;
import co.judisa.service.EmpleadoService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class EmpleadoServiceImpl implements EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final CorteRepository corteRepository;
    private final PagoRepository pagoRepository;
    private final EmpleadoMapper mapper;
    private final CorteMapper corteMapper;

    public EmpleadoServiceImpl(EmpleadoRepository empleadoRepository, CorteRepository corteRepository,
                               PagoRepository pagoRepository, EmpleadoMapper mapper, CorteMapper corteMapper) {
        this.empleadoRepository = empleadoRepository;
        this.corteRepository = corteRepository;
        this.pagoRepository = pagoRepository;
        this.mapper = mapper;
        this.corteMapper = corteMapper;
    }

    /** RF-11 listado paginado, RF-15 búsqueda por nombre o documento, RF-16 filtro por tipo de trabajo. */
    @Override
    public PageResponse<EmpleadoResponse> listar(String q, TipoTrabajo tipo, Boolean activo, Pageable pageable) {
        Specification<Empleado> spec = Specification.allOf(
                EmpleadoRepository.busqueda(q), EmpleadoRepository.deTipo(tipo), EmpleadoRepository.conEstado(activo));
        return PageResponse.of(empleadoRepository.findAll(spec, pageable), mapper::toResponse);
    }

    @Override
    public List<EmpleadoResponse> listarActivos() {
        return empleadoRepository.findByActivoTrueOrderByNombreAsc().stream().map(mapper::toResponse).toList();
    }

    @Override
    public EmpleadoResponse obtener(Long id) {
        return mapper.toResponse(buscar(id));
    }

    @Override
    public EmpleadoRequest obtenerParaEditar(Long id) {
        return mapper.toRequest(buscar(id));
    }

    /** RF-8 registrar, RF-12 documento único, RF-13 campos obligatorios (validados en el DTO). */
    @Override
    @Transactional
    public EmpleadoResponse crear(EmpleadoRequest request) {
        if (empleadoRepository.existsByDocumento(request.getDocumento().trim())) {
            throw documentoDuplicado(request.getDocumento());
        }
        Empleado empleado = new Empleado();
        mapper.actualizar(empleado, request);
        return mapper.toResponse(empleadoRepository.saveAndFlush(empleado));
    }

    /** RF-9 modificar, RF-12 el documento no puede pertenecer a otro empleado. */
    @Override
    @Transactional
    public EmpleadoResponse actualizar(Long id, EmpleadoRequest request) {
        Empleado empleado = buscar(id);
        if (empleadoRepository.existsByDocumentoAndIdNot(request.getDocumento().trim(), id)) {
            throw documentoDuplicado(request.getDocumento());
        }
        mapper.actualizar(empleado, request);
        return mapper.toResponse(empleadoRepository.saveAndFlush(empleado));
    }

    /** RF-10 eliminar; RF-14 si tiene cortes o pagos se rechaza (409) y se sugiere desactivarlo. */
    @Override
    @Transactional
    public void eliminar(Long id) {
        Empleado empleado = buscar(id);
        long cortes = corteRepository.countByEmpleadoId(id);
        long pagos = pagoRepository.countByEmpleadoId(id);
        if (cortes > 0 || pagos > 0) {
            throw new BusinessRuleException("El empleado tiene " + cortes + (cortes == 1 ? " corte" : " cortes")
                    + " y " + pagos + (pagos == 1 ? " pago" : " pagos")
                    + " registrados. No se puede eliminar; puede desactivarlo.");
        }
        empleadoRepository.delete(empleado);
    }

    /** RF-14: borrado lógico (desactivar) o reactivación. */
    @Override
    @Transactional
    public EmpleadoResponse cambiarEstado(Long id, boolean activo) {
        Empleado empleado = buscar(id);
        empleado.setActivo(activo);
        return mapper.toResponse(empleadoRepository.saveAndFlush(empleado));
    }

    /** RF-17: detalle con historial de cortes y pagos. */
    @Override
    public EmpleadoDetalleResponse detalle(Long id) {
        Empleado empleado = buscar(id);
        TotalesCortes totales = corteRepository.totalizar(id, LocalDate.of(1900, 1, 1), LocalDate.of(9999, 12, 31));
        return new EmpleadoDetalleResponse(
                mapper.toResponse(empleado),
                totales.cortes(),
                totales.piezas(),
                pagoRepository.countByEmpleadoId(id),
                pagoRepository.totalPagadoAEmpleado(id),
                corteRepository.findTop50ByEmpleadoIdOrderByFechaDescIdDesc(id).stream().map(corteMapper::toResponse).toList(),
                pagoRepository.findTop50ByEmpleadoIdOrderByFechaDescIdDesc(id).stream().map(mapper::toPagoResumen).toList());
    }

    private Empleado buscar(Long id) {
        return empleadoRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.de("El empleado", id));
    }

    private BusinessRuleException documentoDuplicado(String documento) {
        return new BusinessRuleException("Ya existe un empleado con el documento " + documento.trim());
    }
}

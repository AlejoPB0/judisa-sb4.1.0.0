package co.judisa.service;

import co.judisa.dto.comun.PageResponse;
import co.judisa.dto.empleado.EmpleadoDetalleResponse;
import co.judisa.dto.empleado.EmpleadoRequest;
import co.judisa.dto.empleado.EmpleadoResponse;
import co.judisa.model.enums.TipoTrabajo;
import org.springframework.data.domain.Pageable;

import java.util.List;

/** Módulo 2: gestión de empleados (RF-7). */
public interface EmpleadoService {

    PageResponse<EmpleadoResponse> listar(String q, TipoTrabajo tipo, Boolean activo, Pageable pageable);

    List<EmpleadoResponse> listarActivos();

    EmpleadoResponse obtener(Long id);

    EmpleadoRequest obtenerParaEditar(Long id);

    EmpleadoResponse crear(EmpleadoRequest request);

    EmpleadoResponse actualizar(Long id, EmpleadoRequest request);

    void eliminar(Long id);

    EmpleadoResponse cambiarEstado(Long id, boolean activo);

    EmpleadoDetalleResponse detalle(Long id);
}

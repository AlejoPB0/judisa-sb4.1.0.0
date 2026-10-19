package co.judisa.controller.api;

import co.judisa.dto.comun.PageResponse;
import co.judisa.dto.empleado.EmpleadoDetalleResponse;
import co.judisa.dto.empleado.EmpleadoRequest;
import co.judisa.dto.empleado.EmpleadoResponse;
import co.judisa.model.enums.TipoTrabajo;
import co.judisa.service.EmpleadoService;
import co.judisa.util.Paginacion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/empleados")
@Tag(name = "Empleados", description = "RF-7 a RF-17")
public class EmpleadoApiController {

    static final Set<String> ORDENABLES = Set.of("nombre", "documento", "tipoTrabajo", "createdAt");

    private final EmpleadoService empleadoService;

    public EmpleadoApiController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @GetMapping
    @Operation(summary = "Listar empleados (RF-11, RF-15, RF-16)")
    public PageResponse<EmpleadoResponse> listar(@RequestParam(required = false) String q,
                                                 @RequestParam(required = false) TipoTrabajo tipo,
                                                 @RequestParam(required = false) Boolean activo,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size,
                                                 @RequestParam(required = false) String sort) {
        return empleadoService.listar(q, tipo, activo,
                Paginacion.crear(page, size, sort, ORDENABLES, Sort.by("nombre", "id")));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un empleado")
    public EmpleadoResponse obtener(@PathVariable Long id) {
        return empleadoService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar empleado (RF-8, RF-12, RF-13)")
    public EmpleadoResponse crear(@Valid @RequestBody EmpleadoRequest request) {
        return empleadoService.crear(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar empleado (RF-9)")
    public EmpleadoResponse actualizar(@PathVariable Long id, @Valid @RequestBody EmpleadoRequest request) {
        return empleadoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar empleado (RF-10, RF-14)", description = "409 si tiene cortes o pagos asociados")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        empleadoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/desactivar")
    @Operation(summary = "Desactivar empleado (borrado lógico, RF-14)")
    public EmpleadoResponse desactivar(@PathVariable Long id) {
        return empleadoService.cambiarEstado(id, false);
    }

    @PatchMapping("/{id}/activar")
    @Operation(summary = "Reactivar empleado")
    public EmpleadoResponse activar(@PathVariable Long id) {
        return empleadoService.cambiarEstado(id, true);
    }

    @GetMapping("/{id}/historial")
    @Operation(summary = "Detalle del empleado con historial de cortes y pagos (RF-17)")
    public EmpleadoDetalleResponse historial(@PathVariable Long id) {
        return empleadoService.detalle(id);
    }
}

package co.judisa.controller.api;

import co.judisa.dto.comun.PageResponse;
import co.judisa.dto.referencia.ReferenciaRequest;
import co.judisa.dto.referencia.ReferenciaResponse;
import co.judisa.service.ReferenciaService;
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
@RequestMapping("/api/v1/referencias")
@Tag(name = "Referencias")
public class ReferenciaApiController {

    static final Set<String> ORDENABLES = Set.of("codigo", "descripcion", "valorUnitario", "createdAt");

    private final ReferenciaService referenciaService;

    public ReferenciaApiController(ReferenciaService referenciaService) {
        this.referenciaService = referenciaService;
    }

    @GetMapping
    @Operation(summary = "Listar referencias", description = "Para selectores use ?activa=true&size=200")
    public PageResponse<ReferenciaResponse> listar(@RequestParam(required = false) String q,
                                                   @RequestParam(required = false) Boolean activa,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size,
                                                   @RequestParam(required = false) String sort) {
        return referenciaService.listar(q, activa,
                Paginacion.crear(page, size, sort, ORDENABLES, Sort.by("codigo", "id")));
    }

    @GetMapping("/{id}")
    public ReferenciaResponse obtener(@PathVariable Long id) {
        return referenciaService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear referencia")
    public ReferenciaResponse crear(@Valid @RequestBody ReferenciaRequest request) {
        return referenciaService.crear(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar referencia")
    public ReferenciaResponse actualizar(@PathVariable Long id, @Valid @RequestBody ReferenciaRequest request) {
        return referenciaService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar referencia", description = "409 si tiene cortes asociados")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        referenciaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/desactivar")
    public ReferenciaResponse desactivar(@PathVariable Long id) {
        return referenciaService.cambiarEstado(id, false);
    }

    @PatchMapping("/{id}/activar")
    public ReferenciaResponse activar(@PathVariable Long id) {
        return referenciaService.cambiarEstado(id, true);
    }
}

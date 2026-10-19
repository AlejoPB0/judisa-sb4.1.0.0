package co.judisa.controller.api;

import co.judisa.dto.comun.PageResponse;
import co.judisa.dto.corte.CorteFiltro;
import co.judisa.dto.corte.CorteRequest;
import co.judisa.dto.corte.CorteResponse;
import co.judisa.dto.corte.TotalPiezasResponse;
import co.judisa.service.CorteService;
import co.judisa.util.Paginacion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/cortes")
@Tag(name = "Cortes", description = "RF-18 a RF-28")
public class CorteApiController {

    static final Set<String> ORDENABLES = Set.of("fecha", "cantidad", "createdAt");
    static final Sort ORDEN_POR_DEFECTO = Sort.by(Sort.Direction.DESC, "fecha", "id");

    private final CorteService corteService;

    public CorteApiController(CorteService corteService) {
        this.corteService = corteService;
    }

    @GetMapping
    @Operation(summary = "Listar cortes (RF-22, RF-25, RF-26)", description = "sort=fecha,asc o sort=fecha,desc")
    public PageResponse<CorteResponse> listar(
            @RequestParam(required = false) Long empleadoId,
            @RequestParam(required = false) String referencia,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        return corteService.listar(new CorteFiltro(empleadoId, referencia, desde, hasta),
                Paginacion.crear(page, size, sort, ORDENABLES, ORDEN_POR_DEFECTO));
    }

    @GetMapping("/total-piezas")
    @Operation(summary = "Total de piezas por empleado y periodo (RF-27)")
    public TotalPiezasResponse totalPiezas(
            @RequestParam(required = false) Long empleadoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return corteService.totalPiezas(empleadoId, desde, hasta);
    }

    @GetMapping("/{id}")
    public CorteResponse obtener(@PathVariable Long id) {
        return corteService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar corte (RF-19, RF-23, RF-24, RF-28)")
    public CorteResponse crear(@Valid @RequestBody CorteRequest request) {
        return corteService.crear(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar corte (RF-20)")
    public CorteResponse actualizar(@PathVariable Long id, @Valid @RequestBody CorteRequest request) {
        return corteService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar corte (RF-21)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        corteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

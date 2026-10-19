package co.judisa.controller.api;

import co.judisa.dto.comun.PageResponse;
import co.judisa.dto.historial.HistorialFiltro;
import co.judisa.dto.historial.MovimientoHistorialDTO;
import co.judisa.model.enums.TipoMovimiento;
import co.judisa.service.HistorialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/historial")
@Tag(name = "Historial", description = "RF-40 a RF-43")
public class HistorialApiController {

    private final HistorialService historialService;

    public HistorialApiController(HistorialService historialService) {
        this.historialService = historialService;
    }

    @GetMapping
    @Operation(summary = "Historial de cortes y pagos (RF-40 a RF-43)", description = "Orden cronológico descendente")
    public PageResponse<MovimientoHistorialDTO> listar(
            @RequestParam(required = false) Long empleadoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) TipoMovimiento tipo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return historialService.listar(new HistorialFiltro(empleadoId, desde, hasta, tipo), page, size);
    }
}

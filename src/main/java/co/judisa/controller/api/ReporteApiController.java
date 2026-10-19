package co.judisa.controller.api;

import co.judisa.dto.reporte.ArchivoReporte;
import co.judisa.dto.reporte.FormatoReporte;
import co.judisa.dto.reporte.ReporteResumenResponse;
import co.judisa.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reportes")
@Tag(name = "Reportes", description = "RF-44 a RF-47")
public class ReporteApiController {

    private final ReporteService reporteService;

    public ReporteApiController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/resumen")
    @Operation(summary = "Resumen de producción y pagos (RF-44, RF-45, RF-47)", description = "Por defecto, el mes en curso")
    public ReporteResumenResponse resumen(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long empleadoId) {
        return reporteService.resumen(desde, hasta, empleadoId);
    }

    @GetMapping
    @Operation(summary = "Descargar el reporte en PDF o Excel (RF-46)", description = "formato=pdf (por defecto) o xlsx")
    public ResponseEntity<byte[]> descargar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long empleadoId,
            @RequestParam(required = false) String formato) {
        return respuestaArchivo(reporteService.generar(desde, hasta, empleadoId, FormatoReporte.desde(formato)));
    }

    public static ResponseEntity<byte[]> respuestaArchivo(ArchivoReporte archivo) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivo.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(archivo.nombre()).build().toString())
                .body(archivo.contenido());
    }
}

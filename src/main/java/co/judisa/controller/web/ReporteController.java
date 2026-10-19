package co.judisa.controller.web;

import co.judisa.controller.api.ReporteApiController;
import co.judisa.dto.reporte.FormatoReporte;
import co.judisa.exception.FieldValidationException;
import co.judisa.exception.ResourceNotFoundException;
import co.judisa.service.EmpleadoService;
import co.judisa.service.ReporteService;
import co.judisa.util.Paginacion;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Set;

/** Vista web de reportes con vista previa y descarga (RF-44 a RF-47). */
@Controller
@RequestMapping("/reportes")
public class ReporteController {

    private final ReporteService reporteService;
    private final EmpleadoService empleadoService;

    public ReporteController(ReporteService reporteService, EmpleadoService empleadoService) {
        this.reporteService = reporteService;
        this.empleadoService = empleadoService;
    }

    @GetMapping
    public String ver(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                      @RequestParam(required = false) Long empleadoId,
                      Model model) {
        model.addAttribute("menu", "reportes");
        model.addAttribute("empleadoId", empleadoId);
        model.addAttribute("empleados", empleadoService.listar(null, null, null,
                Paginacion.crear(0, Paginacion.TAMANO_MAXIMO, null, Set.of(), Sort.by("nombre"))).content());
        try {
            var reporte = reporteService.resumen(desde, hasta, empleadoId);
            model.addAttribute("reporte", reporte);
            model.addAttribute("desde", reporte.desde());
            model.addAttribute("hasta", reporte.hasta());
        } catch (FieldValidationException | ResourceNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("desde", desde);
            model.addAttribute("hasta", hasta);
        }
        return "reportes/index";
    }

    /** RF-46: descarga en PDF o Excel. */
    @GetMapping("/descargar")
    public Object descargar(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                            @RequestParam(required = false) Long empleadoId,
                            @RequestParam(required = false) String formato,
                            RedirectAttributes flash) {
        try {
            ResponseEntity<byte[]> archivo = ReporteApiController.respuestaArchivo(
                    reporteService.generar(desde, hasta, empleadoId, FormatoReporte.desde(formato)));
            return archivo;
        } catch (FieldValidationException | ResourceNotFoundException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/reportes";
        }
    }
}

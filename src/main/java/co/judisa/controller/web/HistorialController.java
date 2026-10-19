package co.judisa.controller.web;

import co.judisa.dto.historial.HistorialFiltro;
import co.judisa.exception.FieldValidationException;
import co.judisa.model.enums.TipoMovimiento;
import co.judisa.service.EmpleadoService;
import co.judisa.service.HistorialService;
import co.judisa.util.Paginacion;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Set;

/** Vista web del historial de producción y pagos (RF-40 a RF-43). */
@Controller
@RequestMapping("/historial")
public class HistorialController {

    private static final int TAMANO_PAGINA = 20;

    private final HistorialService historialService;
    private final EmpleadoService empleadoService;

    public HistorialController(HistorialService historialService, EmpleadoService empleadoService) {
        this.historialService = historialService;
        this.empleadoService = empleadoService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) Long empleadoId,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                         @RequestParam(required = false) TipoMovimiento tipo,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {
        model.addAttribute("menu", "historial");
        model.addAttribute("empleadoId", empleadoId);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("tipo", tipo);
        model.addAttribute("tipos", TipoMovimiento.values());
        model.addAttribute("empleados", empleadoService.listar(null, null, null,
                Paginacion.crear(0, Paginacion.TAMANO_MAXIMO, null, Set.of(), Sort.by("nombre"))).content());
        try {
            model.addAttribute("pagina", historialService.listar(new HistorialFiltro(empleadoId, desde, hasta, tipo),
                    page, TAMANO_PAGINA));
        } catch (FieldValidationException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "historial/lista";
    }
}

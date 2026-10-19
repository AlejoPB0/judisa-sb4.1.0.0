package co.judisa.controller.web;

import co.judisa.dto.empleado.EmpleadoRequest;
import co.judisa.exception.BusinessRuleException;
import co.judisa.model.enums.TipoTrabajo;
import co.judisa.service.EmpleadoService;
import co.judisa.util.Paginacion;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

/** Vistas web del módulo de empleados (RF-7 a RF-17); usa los mismos servicios que la API. */
@Controller
@RequestMapping("/empleados")
public class EmpleadoController {

    private static final int TAMANO_PAGINA = 15;

    private final EmpleadoService empleadoService;

    public EmpleadoController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @ModelAttribute("menu")
    public String menu() {
        return "empleados";
    }

    @ModelAttribute("tiposTrabajo")
    public TipoTrabajo[] tiposTrabajo() {
        return TipoTrabajo.values();
    }

    /** RF-11, RF-15, RF-16. */
    @GetMapping
    public String listar(@RequestParam(required = false) String q,
                         @RequestParam(required = false) TipoTrabajo tipo,
                         @RequestParam(required = false) Boolean activo,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {
        model.addAttribute("pagina", empleadoService.listar(q, tipo, activo,
                Paginacion.crear(page, TAMANO_PAGINA, null, Set.of(), Sort.by("nombre", "id"))));
        model.addAttribute("q", q);
        model.addAttribute("tipo", tipo);
        model.addAttribute("activo", activo);
        return "empleados/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("form", new EmpleadoRequest());
        return "empleados/formulario";
    }

    /** RF-8, RF-12, RF-13. */
    @PostMapping
    public String crear(@Valid @ModelAttribute("form") EmpleadoRequest form, BindingResult result,
                        RedirectAttributes flash) {
        if (result.hasErrors()) {
            return "empleados/formulario";
        }
        try {
            var creado = empleadoService.crear(form);
            flash.addFlashAttribute("exito", "Empleado " + creado.nombre() + " registrado.");
            return "redirect:/empleados";
        } catch (BusinessRuleException e) {
            result.rejectValue("documento", "duplicado", e.getMessage());
            return "empleados/formulario";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("form", empleadoService.obtenerParaEditar(id));
        model.addAttribute("empleadoId", id);
        return "empleados/formulario";
    }

    /** RF-9. */
    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("form") EmpleadoRequest form,
                             BindingResult result, Model model, RedirectAttributes flash) {
        model.addAttribute("empleadoId", id);
        if (result.hasErrors()) {
            return "empleados/formulario";
        }
        try {
            empleadoService.actualizar(id, form);
            flash.addFlashAttribute("exito", "Datos del empleado actualizados.");
            return "redirect:/empleados/" + id;
        } catch (BusinessRuleException e) {
            result.rejectValue("documento", "duplicado", e.getMessage());
            return "empleados/formulario";
        }
    }

    /** RF-17: detalle con historial de cortes y pagos. */
    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("detalle", empleadoService.detalle(id));
        return "empleados/detalle";
    }

    /** RF-10, RF-14: si tiene registros asociados se informa y se ofrece desactivarlo. */
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            empleadoService.eliminar(id);
            flash.addFlashAttribute("exito", "Empleado eliminado.");
            return "redirect:/empleados";
        } catch (BusinessRuleException e) {
            flash.addFlashAttribute("error", e.getMessage());
            flash.addFlashAttribute("ofrecerDesactivar", id);
            return "redirect:/empleados/" + id;
        }
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(@PathVariable Long id, RedirectAttributes flash) {
        empleadoService.cambiarEstado(id, false);
        flash.addFlashAttribute("exito", "Empleado desactivado. Su historial se conserva.");
        return "redirect:/empleados/" + id;
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Long id, RedirectAttributes flash) {
        empleadoService.cambiarEstado(id, true);
        flash.addFlashAttribute("exito", "Empleado activado.");
        return "redirect:/empleados/" + id;
    }
}

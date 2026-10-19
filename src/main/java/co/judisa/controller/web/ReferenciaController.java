package co.judisa.controller.web;

import co.judisa.dto.referencia.ReferenciaRequest;
import co.judisa.exception.BusinessRuleException;
import co.judisa.service.ReferenciaService;
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

@Controller
@RequestMapping("/referencias")
public class ReferenciaController {

    private static final int TAMANO_PAGINA = 15;

    private final ReferenciaService referenciaService;

    public ReferenciaController(ReferenciaService referenciaService) {
        this.referenciaService = referenciaService;
    }

    @ModelAttribute("menu")
    public String menu() {
        return "referencias";
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String q,
                         @RequestParam(required = false) Boolean activa,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {
        model.addAttribute("pagina", referenciaService.listar(q, activa,
                Paginacion.crear(page, TAMANO_PAGINA, null, Set.of(), Sort.by("codigo", "id"))));
        model.addAttribute("q", q);
        model.addAttribute("activa", activa);
        return "referencias/lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("form", new ReferenciaRequest());
        return "referencias/formulario";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("form") ReferenciaRequest form, BindingResult result,
                        RedirectAttributes flash) {
        if (result.hasErrors()) {
            return "referencias/formulario";
        }
        try {
            var creada = referenciaService.crear(form);
            flash.addFlashAttribute("exito", "Referencia " + creada.codigo() + " creada.");
            return "redirect:/referencias";
        } catch (BusinessRuleException e) {
            result.rejectValue("codigo", "duplicado", e.getMessage());
            return "referencias/formulario";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("form", referenciaService.obtenerParaEditar(id));
        model.addAttribute("referenciaId", id);
        return "referencias/formulario";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("form") ReferenciaRequest form,
                             BindingResult result, Model model, RedirectAttributes flash) {
        model.addAttribute("referenciaId", id);
        if (result.hasErrors()) {
            return "referencias/formulario";
        }
        try {
            referenciaService.actualizar(id, form);
            flash.addFlashAttribute("exito", "Referencia actualizada.");
            return "redirect:/referencias";
        } catch (BusinessRuleException e) {
            result.rejectValue("codigo", "duplicado", e.getMessage());
            return "referencias/formulario";
        }
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            referenciaService.eliminar(id);
            flash.addFlashAttribute("exito", "Referencia eliminada.");
        } catch (BusinessRuleException e) {
            flash.addFlashAttribute("error", e.getMessage());
            flash.addFlashAttribute("ofrecerDesactivar", id);
        }
        return "redirect:/referencias";
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(@PathVariable Long id, RedirectAttributes flash) {
        referenciaService.cambiarEstado(id, false);
        flash.addFlashAttribute("exito", "Referencia desactivada; ya no aparece al registrar cortes.");
        return "redirect:/referencias";
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Long id, RedirectAttributes flash) {
        referenciaService.cambiarEstado(id, true);
        flash.addFlashAttribute("exito", "Referencia activada.");
        return "redirect:/referencias";
    }
}

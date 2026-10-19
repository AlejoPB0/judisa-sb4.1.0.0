package co.judisa.controller.web;

import co.judisa.dto.corte.CorteFiltro;
import co.judisa.dto.corte.CorteRequest;
import co.judisa.dto.empleado.EmpleadoResponse;
import co.judisa.dto.referencia.ReferenciaResponse;
import co.judisa.exception.BusinessRuleException;
import co.judisa.exception.FieldValidationException;
import co.judisa.exception.ResourceNotFoundException;
import co.judisa.service.CorteService;
import co.judisa.service.EmpleadoService;
import co.judisa.service.ReferenciaService;
import co.judisa.util.Formatos;
import co.judisa.util.Paginacion;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Vistas web del módulo de cortes (RF-18 a RF-28). */
@Controller
@RequestMapping("/cortes")
public class CorteController {

    private static final int TAMANO_PAGINA = 15;
    private static final Set<String> ORDENABLES = Set.of("fecha");

    private final CorteService corteService;
    private final EmpleadoService empleadoService;
    private final ReferenciaService referenciaService;

    public CorteController(CorteService corteService, EmpleadoService empleadoService,
                           ReferenciaService referenciaService) {
        this.corteService = corteService;
        this.empleadoService = empleadoService;
        this.referenciaService = referenciaService;
    }

    @ModelAttribute("menu")
    public String menu() {
        return "cortes";
    }

    /** RF-22 filtros, RF-25 orden por fecha, RF-26 búsqueda por referencia, RF-27 total de piezas. */
    @GetMapping
    public String listar(@RequestParam(required = false) Long empleadoId,
                         @RequestParam(required = false) String referencia,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                         @RequestParam(defaultValue = "fecha,desc") String sort,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {
        model.addAttribute("empleadoId", empleadoId);
        model.addAttribute("referencia", referencia);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("ascendente", sort.endsWith(",asc"));
        model.addAttribute("empleados", empleadoService.listar(null, null, null,
                Paginacion.crear(0, Paginacion.TAMANO_MAXIMO, null, Set.of(), Sort.by("nombre"))).content());
        try {
            model.addAttribute("pagina", corteService.listar(new CorteFiltro(empleadoId, referencia, desde, hasta),
                    Paginacion.crear(page, TAMANO_PAGINA, sort, ORDENABLES, Sort.by(Sort.Direction.DESC, "fecha", "id"))));
            model.addAttribute("total", corteService.totalPiezas(empleadoId, desde, hasta));
        } catch (FieldValidationException | ResourceNotFoundException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "cortes/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(@RequestParam(required = false) Long empleadoId, Model model) {
        CorteRequest form = new CorteRequest();
        form.setFecha(Formatos.hoy());
        form.setEmpleadoId(empleadoId);
        model.addAttribute("form", form);
        return formulario(model, form);
    }

    /** RF-19, RF-23, RF-24, RF-28. */
    @PostMapping
    public String crear(@Valid @ModelAttribute("form") CorteRequest form, BindingResult result,
                        Model model, RedirectAttributes flash) {
        if (!result.hasErrors()) {
            try {
                corteService.crear(form);
                flash.addFlashAttribute("exito", "Corte registrado.");
                return "redirect:/cortes";
            } catch (BusinessRuleException | ResourceNotFoundException e) {
                result.reject("negocio", e.getMessage());
            }
        }
        return formulario(model, form);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        CorteRequest form = corteService.obtenerParaEditar(id);
        model.addAttribute("form", form);
        model.addAttribute("corteId", id);
        return formulario(model, form);
    }

    /** RF-20. */
    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("form") CorteRequest form,
                             BindingResult result, Model model, RedirectAttributes flash) {
        model.addAttribute("corteId", id);
        if (!result.hasErrors()) {
            try {
                corteService.actualizar(id, form);
                flash.addFlashAttribute("exito", "Corte actualizado.");
                return "redirect:/cortes";
            } catch (BusinessRuleException e) {
                result.reject("negocio", e.getMessage());
            }
        }
        return formulario(model, form);
    }

    /** RF-21. */
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        corteService.eliminar(id);
        flash.addFlashAttribute("exito", "Corte eliminado.");
        return "redirect:/cortes";
    }

    /** Opciones de los selectores: activos más el valor actual del corte aunque hoy esté inactivo. */
    private String formulario(Model model, CorteRequest form) {
        List<EmpleadoResponse> empleados = new ArrayList<>(empleadoService.listarActivos());
        if (form.getEmpleadoId() != null && empleados.stream().noneMatch(e -> e.id().equals(form.getEmpleadoId()))) {
            try {
                empleados.add(empleadoService.obtener(form.getEmpleadoId()));
            } catch (ResourceNotFoundException ignorado) {
                form.setEmpleadoId(null);
            }
        }
        List<ReferenciaResponse> referencias = new ArrayList<>(referenciaService.listarActivas());
        if (form.getReferenciaId() != null && referencias.stream().noneMatch(r -> r.id().equals(form.getReferenciaId()))) {
            try {
                referencias.add(referenciaService.obtener(form.getReferenciaId()));
            } catch (ResourceNotFoundException ignorado) {
                form.setReferenciaId(null);
            }
        }
        model.addAttribute("empleados", empleados);
        model.addAttribute("referencias", referencias);
        model.addAttribute("hoy", Formatos.hoy());
        return "cortes/formulario";
    }
}

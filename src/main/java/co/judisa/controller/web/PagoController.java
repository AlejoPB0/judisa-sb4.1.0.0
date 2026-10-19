package co.judisa.controller.web;

import co.judisa.dto.empleado.EmpleadoResponse;
import co.judisa.dto.pago.CambioEstadoPagoRequest;
import co.judisa.dto.pago.PagoFiltro;
import co.judisa.dto.pago.PagoRequest;
import co.judisa.exception.BusinessRuleException;
import co.judisa.exception.FieldValidationException;
import co.judisa.exception.ResourceNotFoundException;
import co.judisa.model.enums.EstadoPago;
import co.judisa.service.EmpleadoService;
import co.judisa.service.PagoService;
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

/** Vistas web del módulo de pagos (RF-29 a RF-39). */
@Controller
@RequestMapping("/pagos")
public class PagoController {

    private static final int TAMANO_PAGINA = 15;
    private static final Set<String> ORDENABLES = Set.of("fecha", "total");

    private final PagoService pagoService;
    private final EmpleadoService empleadoService;
    private final ReferenciaService referenciaService;

    public PagoController(PagoService pagoService, EmpleadoService empleadoService, ReferenciaService referenciaService) {
        this.pagoService = pagoService;
        this.empleadoService = empleadoService;
        this.referenciaService = referenciaService;
    }

    @ModelAttribute("menu")
    public String menu() {
        return "pagos";
    }

    /** RF-33 filtros, RF-37 total pagado del periodo, RF-39 saldo pendiente cuando se elige un empleado. */
    @GetMapping
    public String listar(@RequestParam(required = false) Long empleadoId,
                         @RequestParam(required = false) EstadoPago estado,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                         @RequestParam(defaultValue = "fecha,desc") String sort,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {
        model.addAttribute("empleadoId", empleadoId);
        model.addAttribute("estado", estado);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("ascendente", sort.endsWith(",asc"));
        model.addAttribute("estados", EstadoPago.values());
        model.addAttribute("empleados", todosLosEmpleados());
        try {
            model.addAttribute("pagina", pagoService.listar(new PagoFiltro(empleadoId, estado, desde, hasta),
                    Paginacion.crear(page, TAMANO_PAGINA, sort, ORDENABLES, Sort.by(Sort.Direction.DESC, "fecha", "id"))));
            model.addAttribute("total", pagoService.total(empleadoId, desde, hasta));
            if (empleadoId != null) {
                model.addAttribute("saldo", pagoService.saldoPendiente(empleadoId, desde, hasta));
            }
        } catch (FieldValidationException | ResourceNotFoundException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "pagos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(@RequestParam(required = false) Long empleadoId, Model model) {
        PagoRequest form = new PagoRequest();
        form.setFecha(Formatos.hoy());
        form.setEmpleadoId(empleadoId);
        form.setEstado(EstadoPago.PENDIENTE);
        model.addAttribute("form", form);
        return formulario(model, form);
    }

    /** RF-30, RF-34, RF-35, RF-36. */
    @PostMapping
    public String crear(@Valid @ModelAttribute("form") PagoRequest form, BindingResult result,
                        Model model, RedirectAttributes flash) {
        if (!result.hasErrors()) {
            try {
                pagoService.crear(form);
                flash.addFlashAttribute("exito", "Pago registrado.");
                return "redirect:/pagos";
            } catch (BusinessRuleException | ResourceNotFoundException e) {
                result.reject("negocio", e.getMessage());
            } catch (FieldValidationException e) {
                result.rejectValue(e.getCampo(), "negocio", e.getMessage());
            }
        }
        return formulario(model, form);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        PagoRequest form = pagoService.obtenerParaEditar(id);
        model.addAttribute("form", form);
        model.addAttribute("pagoId", id);
        return formulario(model, form);
    }

    /** RF-31. */
    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("form") PagoRequest form,
                             BindingResult result, Model model, RedirectAttributes flash) {
        model.addAttribute("pagoId", id);
        if (!result.hasErrors()) {
            try {
                pagoService.actualizar(id, form);
                flash.addFlashAttribute("exito", "Pago actualizado.");
                return "redirect:/pagos";
            } catch (BusinessRuleException e) {
                result.reject("negocio", e.getMessage());
            } catch (FieldValidationException e) {
                result.rejectValue(e.getCampo(), "negocio", e.getMessage());
            }
        }
        return formulario(model, form);
    }

    /** RF-38: marcar como realizado (hoy) o volver a pendiente. */
    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, @RequestParam EstadoPago estado,
                                @RequestParam(required = false) String volver, RedirectAttributes flash) {
        pagoService.cambiarEstado(id, new CambioEstadoPagoRequest(estado, null));
        flash.addFlashAttribute("exito", estado == EstadoPago.REALIZADO
                ? "Pago marcado como realizado." : "Pago marcado como pendiente.");
        return "redirect:" + destinoSeguro(volver);
    }

    /** RF-32. */
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        pagoService.eliminar(id);
        flash.addFlashAttribute("exito", "Pago eliminado.");
        return "redirect:/pagos";
    }

    /** Solo se permite volver a rutas internas de la lista de pagos (evita redirecciones abiertas). */
    static String destinoSeguro(String volver) {
        if (volver != null && volver.startsWith("/pagos") && !volver.startsWith("//") && !volver.contains("\\")
                && !volver.contains("\r") && !volver.contains("\n")) {
            return volver;
        }
        return "/pagos";
    }

    private List<EmpleadoResponse> todosLosEmpleados() {
        return empleadoService.listar(null, null, null,
                Paginacion.crear(0, Paginacion.TAMANO_MAXIMO, null, Set.of(), Sort.by("nombre"))).content();
    }

    /** Opciones: empleados activos más el actual del pago aunque hoy esté inactivo; referencias para sugerir el valor. */
    private String formulario(Model model, PagoRequest form) {
        List<EmpleadoResponse> empleados = new ArrayList<>(empleadoService.listarActivos());
        if (form.getEmpleadoId() != null && empleados.stream().noneMatch(e -> e.id().equals(form.getEmpleadoId()))) {
            try {
                empleados.add(empleadoService.obtener(form.getEmpleadoId()));
            } catch (ResourceNotFoundException ignorado) {
                form.setEmpleadoId(null);
            }
        }
        model.addAttribute("empleados", empleados);
        model.addAttribute("referencias", referenciaService.listarActivas());
        model.addAttribute("estados", EstadoPago.values());
        model.addAttribute("hoy", Formatos.hoy());
        return "pagos/formulario";
    }
}

package co.judisa.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Errores de las vistas web: se muestran en una página amigable en español. */
@ControllerAdvice(basePackages = "co.judisa.controller.web")
public class WebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(WebExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String noEncontrado(ResourceNotFoundException ex, Model model) {
        return pagina(model, "No encontrado", ex.getMessage());
    }

    @ExceptionHandler(BusinessRuleException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String reglaNegocio(BusinessRuleException ex, Model model) {
        return pagina(model, "No se pudo completar la operación", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String inesperado(Exception ex, Model model) {
        log.error("Error no controlado en la web", ex);
        return pagina(model, "Error inesperado", "Ocurrió un error inesperado. Intente de nuevo.");
    }

    private String pagina(Model model, String titulo, String mensaje) {
        model.addAttribute("tituloError", titulo);
        model.addAttribute("mensajeError", mensaje);
        return "error/general";
    }
}

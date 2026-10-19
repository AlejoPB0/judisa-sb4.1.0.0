package co.judisa.controller.web;

import co.judisa.util.Enlaces;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Atributos disponibles en todas las vistas web. */
@ControllerAdvice(basePackages = "co.judisa.controller.web")
public class ModeloComunAdvice {

    private final Enlaces enlaces;

    public ModeloComunAdvice(Enlaces enlaces) {
        this.enlaces = enlaces;
    }

    @ModelAttribute("enlaces")
    public Enlaces enlaces() {
        return enlaces;
    }
}

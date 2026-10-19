package co.judisa.controller.web;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** RF-1, RF-2: formulario de inicio de sesión (el POST /login lo procesa Spring Security). */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }
        return "auth/login";
    }
}

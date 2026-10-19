package co.judisa.controller.web;

import co.judisa.dto.auth.CambioPasswordRequest;
import co.judisa.exception.FieldValidationException;
import co.judisa.security.UsuarioPrincipal;
import co.judisa.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** RF-6: cambio de contraseña desde la web (obligatorio en el primer ingreso). */
@Controller
@RequestMapping("/cuenta/password")
public class CuentaController {

    private final AuthService authService;
    private final SecurityContextRepository securityContextRepository;

    public CuentaController(AuthService authService, SecurityContextRepository securityContextRepository) {
        this.authService = authService;
        this.securityContextRepository = securityContextRepository;
    }

    @GetMapping
    public String formulario(@AuthenticationPrincipal UsuarioPrincipal usuario, Model model) {
        model.addAttribute("form", new CambioPasswordRequest());
        model.addAttribute("obligatorio", usuario.isDebeCambiarPassword());
        model.addAttribute("menu", "password");
        return "auth/cambiar-password";
    }

    @PostMapping
    public String cambiar(@AuthenticationPrincipal UsuarioPrincipal usuario,
                          @Valid @ModelAttribute("form") CambioPasswordRequest form, BindingResult result,
                          Model model, RedirectAttributes flash,
                          HttpServletRequest request, HttpServletResponse response) {
        if (!result.hasErrors()) {
            try {
                UsuarioPrincipal actualizado = authService.cambiarPassword(usuario.getId(), form);
                actualizarSesion(actualizado, request, response);
                flash.addFlashAttribute("exito", "La contraseña se cambió correctamente.");
                return "redirect:/";
            } catch (FieldValidationException e) {
                result.rejectValue(e.getCampo(), "invalido", e.getMessage());
            }
        }
        model.addAttribute("obligatorio", usuario.isDebeCambiarPassword());
        model.addAttribute("menu", "password");
        return "auth/cambiar-password";
    }

    /** Reemplaza el usuario guardado en la sesión para que deje de exigir el cambio de contraseña. */
    private void actualizarSesion(UsuarioPrincipal principal, HttpServletRequest request, HttpServletResponse response) {
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                principal, null, principal.getAuthorities()));
        SecurityContextHolder.setContext(contexto);
        securityContextRepository.saveContext(contexto, request, response);
    }
}

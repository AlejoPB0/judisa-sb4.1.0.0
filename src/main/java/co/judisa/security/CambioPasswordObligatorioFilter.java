package co.judisa.security;

import co.judisa.dto.comun.ErrorResponse;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Obliga a cambiar la contraseña inicial antes de usar el sistema.
 * API: responde 403 con error PASSWORD_CHANGE_REQUIRED. Web: redirige a /cuenta/password.
 */
public class CambioPasswordObligatorioFilter extends OncePerRequestFilter {

    public static final String CODIGO = "PASSWORD_CHANGE_REQUIRED";

    private final boolean modoApi;
    private final List<String> rutasPermitidas;
    private final ObjectMapper objectMapper;

    public CambioPasswordObligatorioFilter(boolean modoApi, List<String> rutasPermitidas, ObjectMapper objectMapper) {
        this.modoApi = modoApi;
        this.rutasPermitidas = rutasPermitidas;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal principal
                && principal.isDebeCambiarPassword() && !esPermitida(request)) {
            if (modoApi) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                objectMapper.writeValue(response.getWriter(), new ErrorResponse(OffsetDateTime.now(), 403, CODIGO,
                        "Debe cambiar la contraseña inicial antes de continuar", null));
            } else {
                response.sendRedirect(request.getContextPath() + "/cuenta/password");
            }
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean esPermitida(HttpServletRequest request) {
        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        return rutasPermitidas.stream().anyMatch(ruta::startsWith);
    }
}

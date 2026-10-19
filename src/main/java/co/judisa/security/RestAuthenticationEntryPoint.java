package co.judisa.security;

import co.judisa.dto.comun.ErrorResponse;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

/** Respuestas JSON de 401 y 403 para la API (en lugar de redirigir al formulario de login). */
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
            throws IOException {
        escribir(response, HttpStatus.UNAUTHORIZED, "Sesión no válida o vencida. Inicie sesión de nuevo.");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
            throws IOException {
        escribir(response, HttpStatus.FORBIDDEN, "No tiene permiso para esta operación");
    }

    private void escribir(HttpServletResponse response, HttpStatus status, String mensaje) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(),
                new ErrorResponse(OffsetDateTime.now(), status.value(), status.getReasonPhrase(), mensaje, null));
    }
}

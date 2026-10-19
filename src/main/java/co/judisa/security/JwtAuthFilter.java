package co.judisa.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Autentica las peticiones de la API con la cabecera {@code Authorization: Bearer <jwt>}.
 * Si el token falta o es inválido la petición sigue sin autenticar y el punto de entrada responde 401.
 * No es un @Component para que Spring Boot no lo registre también como filtro global.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera != null && cabecera.startsWith(PREFIJO)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            jwtService.validarYObtenerUsuario(cabecera.substring(PREFIJO.length()).trim())
                    .ifPresent(username -> autenticar(username, request));
        }
        chain.doFilter(request, response);
    }

    private void autenticar(String username, HttpServletRequest request) {
        try {
            UserDetails usuario = userDetailsService.loadUserByUsername(username);
            if (!usuario.isEnabled()) {
                return;
            }
            var auth = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (UsernameNotFoundException e) {
            // Usuario eliminado después de emitir el token: se trata como no autenticado
        }
    }
}

package co.judisa.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

/** RF-5: en la web el mensaje es genérico; solo se distingue la cuenta bloqueada (RNF4). */
public class WebLoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException ex) throws IOException {
        String parametro = ex instanceof LockedException ? "bloqueado" : "error";
        response.sendRedirect(request.getContextPath() + "/login?" + parametro);
    }
}

package co.judisa.service;

import co.judisa.dto.auth.CambioPasswordRequest;
import co.judisa.dto.auth.LoginRequest;
import co.judisa.dto.auth.TokenResponse;
import co.judisa.security.UsuarioPrincipal;

/** Módulo 1: autenticación y seguridad. */
public interface AuthService {

    TokenResponse login(LoginRequest request);

    TokenResponse refrescar(String refreshToken);

    void cerrarSesion(String refreshToken);

    /** Cambia la contraseña y revoca los tokens de refresco existentes; devuelve el principal actualizado. */
    UsuarioPrincipal cambiarPassword(Long usuarioId, CambioPasswordRequest request);

    /** Emite un nuevo par de tokens para la app móvil. */
    TokenResponse emitirTokens(UsuarioPrincipal principal);
}

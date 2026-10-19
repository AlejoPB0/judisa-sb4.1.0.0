package co.judisa.controller.api;

import co.judisa.dto.auth.CambioPasswordRequest;
import co.judisa.dto.auth.LoginRequest;
import co.judisa.dto.auth.RefreshRequest;
import co.judisa.dto.auth.TokenResponse;
import co.judisa.security.UsuarioPrincipal;
import co.judisa.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "RF-1 a RF-6")
public class AuthApiController {

    private final AuthService authService;

    public AuthApiController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Iniciar sesión (RF-2)", description = "Devuelve JWT de acceso y token de refresco")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    @SecurityRequirements
    @Operation(summary = "Renovar el token de acceso")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refrescar(request.refreshToken());
    }

    @PostMapping("/logout")
    @SecurityRequirements
    @Operation(summary = "Cerrar sesión (RF-3)", description = "Invalida el token de refresco en el servidor")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.cerrarSesion(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/password")
    @Operation(summary = "Cambiar contraseña (RF-6)", description = "Revoca los refresh anteriores y devuelve un nuevo par de tokens")
    public TokenResponse cambiarPassword(@AuthenticationPrincipal UsuarioPrincipal usuario,
                                         @Valid @RequestBody CambioPasswordRequest request) {
        return authService.emitirTokens(authService.cambiarPassword(usuario.getId(), request));
    }
}

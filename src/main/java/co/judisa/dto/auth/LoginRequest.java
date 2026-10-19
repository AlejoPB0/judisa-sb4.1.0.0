package co.judisa.dto.auth;

import jakarta.validation.constraints.NotBlank;

/** RF-2, RF-4: usuario y contraseña obligatorios. */
public record LoginRequest(
        @NotBlank(message = "El usuario es obligatorio") String username,
        @NotBlank(message = "La contraseña es obligatoria") String password) {
}

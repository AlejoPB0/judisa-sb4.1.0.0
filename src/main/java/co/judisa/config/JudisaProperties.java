package co.judisa.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

/** Propiedades propias de la aplicación (prefijo {@code judisa}), alimentadas por variables de entorno. */
@ConfigurationProperties(prefix = "judisa")
public record JudisaProperties(Jwt jwt, Cors cors, Admin admin, Seguridad seguridad) {

    public record Jwt(String secret, Duration accessExpiration, Duration refreshExpiration) {
    }

    public record Cors(List<String> allowedOrigins) {
    }

    public record Admin(String username, String password, String nombreCompleto) {
    }

    public record Seguridad(int maxIntentosFallidos, Duration duracionBloqueo) {
    }
}

package co.judisa.security;

import co.judisa.config.JudisaProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/** Emite y valida los JWT de acceso de la app móvil (HMAC-SHA con clave de la variable JWT_SECRET). */
@Service
public class JwtService {

    private static final String EMISOR = "judisa";

    private final SecretKey clave;
    private final Duration duracionAcceso;

    public JwtService(JudisaProperties props) {
        String secreto = props.jwt().secret();
        if (secreto == null || secreto.isBlank()) {
            throw new IllegalStateException("Falta la variable de entorno JWT_SECRET (Base64, mínimo 32 bytes)");
        }
        byte[] bytes = Decoders.BASE64.decode(secreto);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 256 bits (32 bytes) en Base64");
        }
        this.clave = Keys.hmacShaKeyFor(bytes);
        this.duracionAcceso = props.jwt().accessExpiration();
    }

    public String generarAcceso(UsuarioPrincipal usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .issuer(EMISOR)
                .subject(usuario.getUsername())
                .claim("uid", usuario.getId())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(duracionAcceso)))
                .signWith(clave)
                .compact();
    }

    /** Devuelve el usuario del token si la firma y la vigencia son válidas. */
    public Optional<String> validarYObtenerUsuario(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(clave).requireIssuer(EMISOR).build()
                    .parseSignedClaims(token).getPayload();
            return Optional.ofNullable(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public long segundosDeVigencia() {
        return duracionAcceso.toSeconds();
    }
}

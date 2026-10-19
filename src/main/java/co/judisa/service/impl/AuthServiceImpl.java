package co.judisa.service.impl;

import co.judisa.config.JudisaProperties;
import co.judisa.dto.auth.CambioPasswordRequest;
import co.judisa.dto.auth.LoginRequest;
import co.judisa.dto.auth.TokenResponse;
import co.judisa.exception.FieldValidationException;
import co.judisa.exception.ResourceNotFoundException;
import co.judisa.exception.SesionInvalidaException;
import co.judisa.model.entity.RefreshToken;
import co.judisa.model.entity.Usuario;
import co.judisa.repository.RefreshTokenRepository;
import co.judisa.repository.UsuarioRepository;
import co.judisa.security.JwtService;
import co.judisa.security.UsuarioPrincipal;
import co.judisa.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class AuthServiceImpl implements AuthService {

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final Duration duracionRefresh;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtService jwtService,
                           UsuarioRepository usuarioRepository, RefreshTokenRepository refreshTokenRepository,
                           PasswordEncoder passwordEncoder, JudisaProperties props) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.duracionRefresh = props.jwt().refreshExpiration();
    }

    /**
     * RF-1, RF-2: autentica a la administradora y emite JWT + refresh.
     * RF-5: las credenciales incorrectas producen BadCredentialsException (mensaje genérico en el manejador).
     * noRollbackFor: el conteo de intentos fallidos (RNF4) se guarda aunque la autenticación falle.
     */
    @Override
    @Transactional(noRollbackFor = AuthenticationException.class)
    public TokenResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username().trim(), request.password()));
        return emitirTokens((UsuarioPrincipal) auth.getPrincipal());
    }

    /** Rotación: cada refresh se usa una sola vez y se reemplaza por uno nuevo. */
    @Override
    @Transactional
    public TokenResponse refrescar(String refreshToken) {
        RefreshToken guardado = refreshTokenRepository.findByTokenHash(hash(refreshToken))
                .filter(t -> t.esValido(OffsetDateTime.now()))
                .orElseThrow(() -> new SesionInvalidaException());
        Usuario usuario = guardado.getUsuario();
        if (!usuario.isActivo()) {
            throw new SesionInvalidaException();
        }
        guardado.setRevocado(true);
        return emitirTokens(new UsuarioPrincipal(usuario));
    }

    /** RF-3: invalida el refresh en el servidor (idempotente). */
    @Override
    @Transactional
    public void cerrarSesion(String refreshToken) {
        refreshTokenRepository.findByTokenHash(hash(refreshToken)).ifPresent(t -> t.setRevocado(true));
    }

    /** RF-6: valida la contraseña actual, la longitud mínima y la confirmación. */
    @Override
    @Transactional
    public UsuarioPrincipal cambiarPassword(Long usuarioId, CambioPasswordRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (!passwordEncoder.matches(request.getPasswordActual(), usuario.getPasswordHash())) {
            throw new FieldValidationException("passwordActual", "La contraseña actual no es correcta");
        }
        if (!request.getPasswordNueva().equals(request.getConfirmacion())) {
            throw new FieldValidationException("confirmacion", "La confirmación no coincide con la nueva contraseña");
        }
        if (request.getPasswordNueva().equals(request.getPasswordActual())) {
            throw new FieldValidationException("passwordNueva", "La nueva contraseña debe ser diferente a la actual");
        }
        usuario.setPasswordHash(passwordEncoder.encode(request.getPasswordNueva()));
        usuario.setDebeCambiarPassword(false);
        refreshTokenRepository.revocarTodosDelUsuario(usuario.getId(), OffsetDateTime.now());
        return new UsuarioPrincipal(usuario);
    }

    @Override
    @Transactional
    public TokenResponse emitirTokens(UsuarioPrincipal principal) {
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        String refresh = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RefreshToken token = new RefreshToken();
        token.setUsuario(usuarioRepository.getReferenceById(principal.getId()));
        token.setTokenHash(hash(refresh));
        token.setExpiraEn(OffsetDateTime.now().plus(duracionRefresh));
        refreshTokenRepository.save(token);

        return new TokenResponse(jwtService.generarAcceso(principal), refresh, "Bearer",
                jwtService.segundosDeVigencia(), principal.getUsername(), principal.getNombreCompleto(),
                principal.isDebeCambiarPassword());
    }

    static String hash(String valor) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(sha.digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}

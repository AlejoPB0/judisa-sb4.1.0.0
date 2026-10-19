package co.judisa.service;

import co.judisa.config.JudisaProperties;
import co.judisa.dto.auth.CambioPasswordRequest;
import co.judisa.exception.FieldValidationException;
import co.judisa.model.entity.Usuario;
import co.judisa.repository.RefreshTokenRepository;
import co.judisa.repository.UsuarioRepository;
import co.judisa.security.JwtService;
import co.judisa.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(4);

    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    UsuarioRepository usuarioRepository;
    @Mock
    RefreshTokenRepository refreshTokenRepository;

    AuthServiceImpl servicio;
    Usuario usuario;

    @BeforeEach
    void setUp() {
        var props = new JudisaProperties(
                new JudisaProperties.Jwt("dGVzdC1zb2xvLXBhcmEtcHJ1ZWJhcy1hdXRvbWF0aWNhcy1kZS1qdWRpc2EtY2xhdmUtMzItYnl0ZXM=",
                        Duration.ofMinutes(15), Duration.ofDays(7)),
                new JudisaProperties.Cors(List.of()), null, null);
        servicio = new AuthServiceImpl(authenticationManager, new JwtService(props), usuarioRepository,
                refreshTokenRepository, ENCODER, props);
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("admin");
        usuario.setNombreCompleto("Admin");
        usuario.setPasswordHash(ENCODER.encode("Actual123"));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
    }

    @Test
    void rf6_rechazaPasswordActualIncorrecta() {
        assertThatThrownBy(() -> servicio.cambiarPassword(1L, new CambioPasswordRequest("otra", "Nueva12345", "Nueva12345")))
                .isInstanceOf(FieldValidationException.class)
                .extracting("campo").isEqualTo("passwordActual");
    }

    @Test
    void rf6_rechazaConfirmacionDistinta() {
        assertThatThrownBy(() -> servicio.cambiarPassword(1L, new CambioPasswordRequest("Actual123", "Nueva12345", "Nueva99999")))
                .isInstanceOf(FieldValidationException.class)
                .extracting("campo").isEqualTo("confirmacion");
    }

    @Test
    void rf6_cambiaPasswordQuitaObligacionYRevocaRefresh() {
        usuario.setDebeCambiarPassword(true);
        var principal = servicio.cambiarPassword(1L, new CambioPasswordRequest("Actual123", "Nueva12345", "Nueva12345"));
        assertThat(ENCODER.matches("Nueva12345", usuario.getPasswordHash())).isTrue();
        assertThat(principal.isDebeCambiarPassword()).isFalse();
        verify(refreshTokenRepository).revocarTodosDelUsuario(eq(1L), any());
    }
}

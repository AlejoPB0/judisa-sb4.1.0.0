package co.judisa.security;

import co.judisa.config.JudisaProperties;
import co.judisa.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * RNF4: bloqueo temporal tras 5 intentos fallidos. Escucha los eventos de autenticación,
 * por lo que aplica igual al formulario web y al login de la API.
 */
@Component
public class IntentosLoginListener {

    private static final Logger log = LoggerFactory.getLogger(IntentosLoginListener.class);

    private final UsuarioRepository usuarioRepository;
    private final JudisaProperties.Seguridad config;

    public IntentosLoginListener(UsuarioRepository usuarioRepository, JudisaProperties props) {
        this.usuarioRepository = usuarioRepository;
        this.config = props.seguridad();
    }

    @EventListener
    @Transactional
    public void alFallar(AuthenticationFailureBadCredentialsEvent evento) {
        String username = String.valueOf(evento.getAuthentication().getPrincipal()).trim();
        usuarioRepository.findByUsername(username).ifPresent(u -> {
            int intentos = u.getIntentosFallidos() + 1;
            if (intentos >= config.maxIntentosFallidos()) {
                u.setBloqueadoHasta(OffsetDateTime.now().plus(config.duracionBloqueo()));
                u.setIntentosFallidos(0);
                log.warn("Usuario '{}' bloqueado temporalmente por {} intentos fallidos", username, intentos);
            } else {
                u.setIntentosFallidos(intentos);
            }
        });
    }

    @EventListener
    @Transactional
    public void alAutenticar(AuthenticationSuccessEvent evento) {
        if (evento.getAuthentication().getPrincipal() instanceof UsuarioPrincipal principal) {
            usuarioRepository.findById(principal.getId()).ifPresent(u -> {
                if (u.getIntentosFallidos() != 0 || u.getBloqueadoHasta() != null) {
                    u.setIntentosFallidos(0);
                    u.setBloqueadoHasta(null);
                }
            });
        }
    }
}

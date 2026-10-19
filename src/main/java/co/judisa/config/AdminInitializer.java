package co.judisa.config;

import co.judisa.model.entity.Usuario;
import co.judisa.model.enums.Rol;
import co.judisa.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea la cuenta de la administradora la primera vez que arranca el sistema.
 * La contraseña inicial viene de ADMIN_PASSWORD y debe cambiarse en el primer ingreso.
 */
@Component
public class AdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JudisaProperties.Admin admin;

    public AdminInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                            JudisaProperties props) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.admin = props.admin();
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarioRepository.count() > 0) {
            return;
        }
        if (admin.password() == null || admin.password().length() < 8) {
            throw new IllegalStateException(
                    "No existe ningún usuario. Defina ADMIN_PASSWORD (mínimo 8 caracteres) para crear la administradora.");
        }
        Usuario usuario = new Usuario();
        usuario.setUsername(admin.username());
        usuario.setPasswordHash(passwordEncoder.encode(admin.password()));
        usuario.setNombreCompleto(admin.nombreCompleto());
        usuario.setRol(Rol.ADMIN);
        usuario.setDebeCambiarPassword(true);
        usuarioRepository.save(usuario);
        log.info("Usuario administrador '{}' creado. Deberá cambiar la contraseña en el primer ingreso.", admin.username());
    }
}

package co.judisa.security;

import co.judisa.model.entity.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

/** Usuario autenticado. Guarda solo los datos necesarios (se serializa en la sesión web). */
public class UsuarioPrincipal implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String username;
    private final String passwordHash;
    private final String nombreCompleto;
    private final String rol;
    private final boolean activo;
    private final boolean bloqueado;
    private final boolean debeCambiarPassword;

    public UsuarioPrincipal(Usuario u) {
        this.id = u.getId();
        this.username = u.getUsername();
        this.passwordHash = u.getPasswordHash();
        this.nombreCompleto = u.getNombreCompleto();
        this.rol = u.getRol().name();
        this.activo = u.isActivo();
        this.bloqueado = u.estaBloqueado(OffsetDateTime.now());
        this.debeCambiarPassword = u.isDebeCambiarPassword();
    }

    public Long getId() {
        return id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public boolean isDebeCambiarPassword() {
        return debeCambiarPassword;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !bloqueado;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }
}

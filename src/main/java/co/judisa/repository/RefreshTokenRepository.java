package co.judisa.repository;

import co.judisa.model.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update RefreshToken t set t.revocado = true, t.updatedAt = :ahora where t.usuario.id = :usuarioId and t.revocado = false")
    int revocarTodosDelUsuario(@Param("usuarioId") Long usuarioId, @Param("ahora") OffsetDateTime ahora);

    @Modifying
    @Query("delete from RefreshToken t where t.expiraEn < :limite")
    int eliminarVencidosAntesDe(@Param("limite") OffsetDateTime limite);
}

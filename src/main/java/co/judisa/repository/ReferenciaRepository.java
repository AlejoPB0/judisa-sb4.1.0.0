package co.judisa.repository;

import co.judisa.model.entity.Referencia;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Locale;

public interface ReferenciaRepository extends JpaRepository<Referencia, Long>, JpaSpecificationExecutor<Referencia> {

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Long id);

    long countByActivaTrue();

    java.util.Optional<Referencia> findByClientId(String clientId);

    List<Referencia> findByActivaTrueOrderByCodigoAsc();

    static Specification<Referencia> busqueda(String q) {
        if (q == null || q.isBlank()) {
            return Specification.unrestricted();
        }
        String patron = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("codigo")), patron),
                cb.like(cb.lower(root.get("descripcion")), patron));
    }

    static Specification<Referencia> conEstado(Boolean activa) {
        return activa == null ? Specification.unrestricted() : (root, query, cb) -> cb.equal(root.get("activa"), activa);
    }
}

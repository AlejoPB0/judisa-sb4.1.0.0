package co.judisa.repository;

import co.judisa.model.entity.Empleado;
import co.judisa.model.enums.TipoTrabajo;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Locale;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long>, JpaSpecificationExecutor<Empleado> {

    boolean existsByDocumento(String documento);

    boolean existsByDocumentoAndIdNot(String documento, Long id);

    long countByActivoTrue();

    java.util.Optional<Empleado> findByClientId(String clientId);

    List<Empleado> findByActivoTrueOrderByNombreAsc();

    /** RF-15: búsqueda por nombre (sin distinguir mayúsculas) o documento. */
    static Specification<Empleado> busqueda(String q) {
        if (q == null || q.isBlank()) {
            return Specification.unrestricted();
        }
        String patron = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nombre")), patron),
                cb.like(root.get("documento"), patron));
    }

    /** RF-16: filtro por tipo de trabajo. */
    static Specification<Empleado> deTipo(TipoTrabajo tipo) {
        return tipo == null ? Specification.unrestricted() : (root, query, cb) -> cb.equal(root.get("tipoTrabajo"), tipo);
    }

    static Specification<Empleado> conEstado(Boolean activo) {
        return activo == null ? Specification.unrestricted() : (root, query, cb) -> cb.equal(root.get("activo"), activo);
    }
}

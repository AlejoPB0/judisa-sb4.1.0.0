package co.judisa.repository;

import co.judisa.dto.pago.PagoFiltro;
import co.judisa.dto.pago.TotalesPagos;
import co.judisa.model.entity.Pago;
import co.judisa.model.enums.EstadoPago;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long>, JpaSpecificationExecutor<Pago> {

    @Override
    @EntityGraph(attributePaths = "empleado")
    Page<Pago> findAll(Specification<Pago> spec, Pageable pageable);

    @EntityGraph(attributePaths = "empleado")
    Optional<Pago> findWithEmpleadoById(Long id);

    Optional<Pago> findByClientId(String clientId);

    long countByEmpleadoId(Long empleadoId);

    long countByEstado(EstadoPago estado);

    List<Pago> findTop50ByEmpleadoIdOrderByFechaDescIdDesc(Long empleadoId);

    @Query("select coalesce(sum(p.total), 0) from Pago p where p.empleado.id = :empleadoId and p.estado = co.judisa.model.enums.EstadoPago.REALIZADO")
    BigDecimal totalPagadoAEmpleado(@Param("empleadoId") Long empleadoId);

    @Query("select coalesce(sum(p.total), 0) from Pago p where p.estado = :estado")
    BigDecimal sumarPorEstado(@Param("estado") EstadoPago estado);

    /** RF-37, RF-39: totales por estado; las fechas nunca son nulas (el servicio pone extremos). */
    @Query("""
            select new co.judisa.dto.pago.TotalesPagos(
                coalesce(sum(case when p.estado = co.judisa.model.enums.EstadoPago.REALIZADO then p.total else 0 end), 0),
                coalesce(sum(case when p.estado = co.judisa.model.enums.EstadoPago.REALIZADO then p.piezas else 0 end), 0),
                coalesce(sum(case when p.estado = co.judisa.model.enums.EstadoPago.PENDIENTE then p.total else 0 end), 0),
                coalesce(sum(case when p.estado = co.judisa.model.enums.EstadoPago.PENDIENTE then p.piezas else 0 end), 0),
                count(p))
            from Pago p
            where (:empleadoId is null or p.empleado.id = :empleadoId)
              and p.fecha between :desde and :hasta
            """)
    TotalesPagos totalizar(@Param("empleadoId") Long empleadoId,
                           @Param("desde") LocalDate desde,
                           @Param("hasta") LocalDate hasta);

    /** Pagos por empleado y estado en un periodo: [empleadoId, nombre, estado, total]. */
    @Query("""
            select p.empleado.id, p.empleado.nombre, p.estado, sum(p.total)
            from Pago p
            where (:empleadoId is null or p.empleado.id = :empleadoId)
              and p.fecha between :desde and :hasta
            group by p.empleado.id, p.empleado.nombre, p.estado
            """)
    List<Object[]> totalesPorEmpleadoYEstado(@Param("empleadoId") Long empleadoId,
                                             @Param("desde") LocalDate desde,
                                             @Param("hasta") LocalDate hasta);

    /** RF-33: filtros combinables. */
    static Specification<Pago> conFiltro(PagoFiltro filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();
            if (filtro.empleadoId() != null) {
                predicados.add(cb.equal(root.get("empleado").get("id"), filtro.empleadoId()));
            }
            if (filtro.estado() != null) {
                predicados.add(cb.equal(root.get("estado"), filtro.estado()));
            }
            if (filtro.desde() != null) {
                predicados.add(cb.greaterThanOrEqualTo(root.get("fecha"), filtro.desde()));
            }
            if (filtro.hasta() != null) {
                predicados.add(cb.lessThanOrEqualTo(root.get("fecha"), filtro.hasta()));
            }
            return cb.and(predicados.toArray(Predicate[]::new));
        };
    }
}

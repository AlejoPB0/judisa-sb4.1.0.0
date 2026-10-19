package co.judisa.repository;

import co.judisa.dto.corte.CorteFiltro;
import co.judisa.dto.corte.TotalesCortes;
import co.judisa.model.entity.Corte;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public interface CorteRepository extends JpaRepository<Corte, Long>, JpaSpecificationExecutor<Corte> {

    /** Carga empleado y referencia en la misma consulta para evitar N+1 en los listados. */
    @Override
    @EntityGraph(attributePaths = {"empleado", "referencia"})
    Page<Corte> findAll(Specification<Corte> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"empleado", "referencia"})
    List<Corte> findTop50ByEmpleadoIdOrderByFechaDescIdDesc(Long empleadoId);

    long countByEmpleadoId(Long empleadoId);

    long countByReferenciaId(Long referenciaId);

    /** RF-27: total de piezas y número de cortes; los límites de fecha nunca son nulos (el servicio pone extremos). */
    @Query("""
            select new co.judisa.dto.corte.TotalesCortes(coalesce(sum(c.cantidad), 0), count(c))
            from Corte c
            where (:empleadoId is null or c.empleado.id = :empleadoId)
              and c.fecha between :desde and :hasta
            """)
    TotalesCortes totalizar(@Param("empleadoId") Long empleadoId,
                      @Param("desde") LocalDate desde,
                      @Param("hasta") LocalDate hasta);

    @EntityGraph(attributePaths = {"empleado", "referencia"})
    java.util.Optional<Corte> findWithRelacionesById(Long id);

    java.util.Optional<Corte> findByClientId(String clientId);

    /** RF-39: valor de lo producido = Σ cantidad × valor unitario actual de la referencia. */
    @Query("""
            select coalesce(sum(c.cantidad * c.referencia.valorUnitario), 0)
            from Corte c
            where (:empleadoId is null or c.empleado.id = :empleadoId)
              and c.fecha between :desde and :hasta
            """)
    java.math.BigDecimal valorProducido(@Param("empleadoId") Long empleadoId,
                                        @Param("desde") LocalDate desde,
                                        @Param("hasta") LocalDate hasta);

    /** RF-45: piezas y valor por empleado y referencia: [empleadoId, nombre, codigo, descripcion, piezas, valor]. */
    @Query("""
            select c.empleado.id, c.empleado.nombre, c.referencia.codigo, c.referencia.descripcion,
                   sum(c.cantidad), sum(c.cantidad * c.referencia.valorUnitario)
            from Corte c
            where (:empleadoId is null or c.empleado.id = :empleadoId)
              and c.fecha between :desde and :hasta
            group by c.empleado.id, c.empleado.nombre, c.referencia.codigo, c.referencia.descripcion
            order by c.empleado.nombre, c.referencia.codigo
            """)
    List<Object[]> piezasPorEmpleadoYReferencia(@Param("empleadoId") Long empleadoId,
                                                @Param("desde") LocalDate desde,
                                                @Param("hasta") LocalDate hasta);

    /** Piezas por día de un periodo (para el gráfico semanal del panel): [fecha, piezas]. */
    @Query("select c.fecha, sum(c.cantidad) from Corte c where c.fecha between :desde and :hasta group by c.fecha")
    List<Object[]> piezasPorDia(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    /** RF-22, RF-26: filtros combinables por empleado, código de referencia y rango de fechas. */
    static Specification<Corte> conFiltro(CorteFiltro filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();
            if (filtro.empleadoId() != null) {
                predicados.add(cb.equal(root.get("empleado").get("id"), filtro.empleadoId()));
            }
            if (filtro.referencia() != null && !filtro.referencia().isBlank()) {
                String patron = "%" + filtro.referencia().trim().toLowerCase(Locale.ROOT) + "%";
                predicados.add(cb.like(cb.lower(root.get("referencia").get("codigo")), patron));
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

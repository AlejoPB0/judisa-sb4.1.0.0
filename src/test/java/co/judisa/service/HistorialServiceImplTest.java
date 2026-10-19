package co.judisa.service;

import co.judisa.dto.historial.HistorialFiltro;
import co.judisa.dto.historial.MovimientoHistorialDTO;
import co.judisa.exception.FieldValidationException;
import co.judisa.model.entity.Corte;
import co.judisa.model.entity.Empleado;
import co.judisa.model.entity.Pago;
import co.judisa.model.entity.Referencia;
import co.judisa.model.enums.EstadoPago;
import co.judisa.model.enums.TipoMovimiento;
import co.judisa.repository.CorteRepository;
import co.judisa.repository.PagoRepository;
import co.judisa.service.impl.HistorialServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistorialServiceImplTest {

    @Mock
    CorteRepository corteRepository;
    @Mock
    PagoRepository pagoRepository;

    HistorialServiceImpl servicio;
    Empleado empleado;
    Referencia referencia;

    @BeforeEach
    void setUp() {
        servicio = new HistorialServiceImpl(corteRepository, pagoRepository);
        empleado = new Empleado();
        empleado.setId(1L);
        empleado.setNombre("María");
        referencia = new Referencia();
        referencia.setCodigo("REF-1");
        referencia.setDescripcion("Jean");
        referencia.setValorUnitario(new BigDecimal("1800"));
    }

    private Corte corte(long id, LocalDate fecha, int cantidad) {
        Corte c = new Corte();
        c.setId(id);
        c.setEmpleado(empleado);
        c.setReferencia(referencia);
        c.setFecha(fecha);
        c.setCantidad(cantidad);
        return c;
    }

    private Pago pago(long id, LocalDate fecha, String nota) {
        Pago p = new Pago();
        p.setId(id);
        p.setEmpleado(empleado);
        p.setFecha(fecha);
        p.setPiezas(10);
        p.setTotal(new BigDecimal("18000"));
        p.setNota(nota);
        p.setEstado(EstadoPago.PENDIENTE);
        return p;
    }

    @Test
    @SuppressWarnings("unchecked")
    void mezclaCortesYPagosEnOrdenDescendenteYPagina() {
        LocalDate hoy = LocalDate.of(2026, 10, 18);
        when(corteRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(corte(3, hoy, 100), corte(2, hoy.minusDays(5), 40)), Pageable.ofSize(2), 3));
        when(pagoRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(pago(9, hoy.minusDays(2), null), pago(8, hoy.minusDays(9), "Quincena")), Pageable.ofSize(2), 2));

        var pagina = servicio.listar(new HistorialFiltro(1L, null, null, null), 0, 3);

        assertThat(pagina.totalElements()).isEqualTo(5);
        assertThat(pagina.totalPages()).isEqualTo(2);
        assertThat(pagina.content()).extracting(MovimientoHistorialDTO::fecha)
                .containsExactly(hoy, hoy.minusDays(2), hoy.minusDays(5));
        MovimientoHistorialDTO primero = pagina.content().get(0);
        assertThat(primero.tipo()).isEqualTo(TipoMovimiento.CORTE);
        assertThat(primero.valor()).isEqualByComparingTo("180000");
        assertThat(primero.detalle()).isEqualTo("REF-1 · Jean");
        assertThat(pagina.content().get(1).detalle()).isEqualTo("Pago de producción");
    }

    @Test
    @SuppressWarnings("unchecked")
    void filtroPorTipoSoloConsultaEsaTabla() {
        when(pagoRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), Pageable.ofSize(20), 0));
        servicio.listar(new HistorialFiltro(null, null, null, TipoMovimiento.PAGO), 0, 20);
        verify(corteRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void rangoInvertidoSeRechaza() {
        assertThatThrownBy(() -> servicio.listar(new HistorialFiltro(null, LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 1), null), 0, 20)).isInstanceOf(FieldValidationException.class);
    }
}

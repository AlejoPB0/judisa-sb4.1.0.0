package co.judisa.service;

import co.judisa.dto.corte.TotalesCortes;
import co.judisa.dto.pago.CambioEstadoPagoRequest;
import co.judisa.dto.pago.PagoRequest;
import co.judisa.dto.pago.SaldoPendienteResponse;
import co.judisa.dto.pago.TotalesPagos;
import co.judisa.exception.BusinessRuleException;
import co.judisa.exception.FieldValidationException;
import co.judisa.mapper.PagoMapper;
import co.judisa.model.entity.Empleado;
import co.judisa.model.entity.Pago;
import co.judisa.model.enums.EstadoPago;
import co.judisa.model.enums.TipoTrabajo;
import co.judisa.repository.CorteRepository;
import co.judisa.repository.EmpleadoRepository;
import co.judisa.repository.PagoRepository;
import co.judisa.service.impl.PagoServiceImpl;
import co.judisa.util.Formatos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoServiceImplTest {

    @Mock
    PagoRepository pagoRepository;
    @Mock
    EmpleadoRepository empleadoRepository;
    @Mock
    CorteRepository corteRepository;

    PagoServiceImpl servicio;

    @BeforeEach
    void setUp() {
        servicio = new PagoServiceImpl(pagoRepository, empleadoRepository, corteRepository, new PagoMapper());
    }

    private static Empleado empleado(long id, boolean activo) {
        Empleado e = new Empleado();
        e.setId(id);
        e.setNombre("María");
        e.setDocumento("1" + id);
        e.setTelefono("3000000000");
        e.setTipoTrabajo(TipoTrabajo.CONFECCIONISTA);
        e.setActivo(activo);
        return e;
    }

    @Test
    void totalEsPiezasPorValorUnitario() {
        assertThat(PagoServiceImpl.calcularTotal(300, new BigDecimal("1800"))).isEqualByComparingTo("540000");
        assertThat(servicio.calcular(1_000_000, new BigDecimal("999999999999")).total())
                .isEqualByComparingTo("999999999999000000");
    }

    @Test
    void crearCalculaElTotalYPorDefectoQuedaPendiente() {
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleado(1, true)));
        when(pagoRepository.saveAndFlush(any(Pago.class))).thenAnswer(i -> i.getArgument(0));

        var r = servicio.crear(new PagoRequest(1L, 120, new BigDecimal("2500"), "  Quincena ", LocalDate.of(2026, 10, 1),
                null, LocalDate.of(2026, 10, 2)));

        assertThat(r.total()).isEqualByComparingTo("300000");
        assertThat(r.estado()).isEqualTo(EstadoPago.PENDIENTE);
        assertThat(r.fechaPagoEfectivo()).isNull();
        assertThat(r.nota()).isEqualTo("Quincena");
    }

    @Test
    void realizadoSinFechaEfectivaUsaLaFechaDelPago() {
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleado(1, true)));
        when(pagoRepository.saveAndFlush(any(Pago.class))).thenAnswer(i -> i.getArgument(0));

        var r = servicio.crear(new PagoRequest(1L, 10, new BigDecimal("1000"), null, LocalDate.of(2026, 10, 5),
                EstadoPago.REALIZADO, null));

        assertThat(r.fechaPagoEfectivo()).isEqualTo(LocalDate.of(2026, 10, 5));
    }

    @Test
    void fechaEfectivaAnteriorALaDelPagoSeRechaza() {
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleado(1, true)));
        assertThatThrownBy(() -> servicio.crear(new PagoRequest(1L, 10, new BigDecimal("1000"), null,
                LocalDate.of(2026, 10, 5), EstadoPago.REALIZADO, LocalDate.of(2026, 10, 1))))
                .isInstanceOf(FieldValidationException.class);
    }

    @Test
    void empleadoInactivoNoRecibePagosNuevos() {
        when(empleadoRepository.findById(2L)).thenReturn(Optional.of(empleado(2, false)));
        assertThatThrownBy(() -> servicio.crear(new PagoRequest(2L, 10, new BigDecimal("1000"), null,
                LocalDate.of(2026, 10, 5), null, null)))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("inactivo");
    }

    @Test
    void marcarRealizadoSinFechaUsaHoyYVolverAPendienteLaBorra() {
        Pago pago = new Pago();
        pago.setId(7L);
        pago.setEmpleado(empleado(1, true));
        pago.setPiezas(10);
        pago.setValorUnitario(BigDecimal.TEN);
        pago.setTotal(new BigDecimal("100"));
        pago.setFecha(LocalDate.of(2026, 10, 1));
        when(pagoRepository.findWithEmpleadoById(7L)).thenReturn(Optional.of(pago));
        when(pagoRepository.saveAndFlush(any(Pago.class))).thenAnswer(i -> i.getArgument(0));

        assertThat(servicio.cambiarEstado(7L, new CambioEstadoPagoRequest(EstadoPago.REALIZADO, null)).fechaPagoEfectivo())
                .isEqualTo(Formatos.hoy());
        assertThat(servicio.cambiarEstado(7L, new CambioEstadoPagoRequest(EstadoPago.PENDIENTE, null)).fechaPagoEfectivo())
                .isNull();
    }

    @Test
    void saldoPendienteComparaProducidoContraPagado() {
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleado(1, true)));
        when(corteRepository.totalizar(eq(1L), any(), any())).thenReturn(new TotalesCortes(655L, 5L));
        when(corteRepository.valorProducido(eq(1L), any(), any())).thenReturn(new BigDecimal("1210000"));
        when(pagoRepository.totalizar(eq(1L), any(), any()))
                .thenReturn(new TotalesPagos(new BigDecimal("540000"), 300L, new BigDecimal("144000"), 80L, 3L));

        SaldoPendienteResponse s = servicio.saldoPendiente(1L, null, null);

        assertThat(s.piezasSaldo()).isEqualTo(355);
        assertThat(s.saldoPendiente()).isEqualByComparingTo("670000");
        assertThat(s.totalEnPagosPendientes()).isEqualByComparingTo("144000");
    }

    @Test
    void saldoPendienteExigeEmpleado() {
        assertThatThrownBy(() -> servicio.saldoPendiente(null, null, null)).isInstanceOf(FieldValidationException.class);
    }
}

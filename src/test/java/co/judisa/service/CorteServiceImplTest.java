package co.judisa.service;

import co.judisa.dto.corte.CorteFiltro;
import co.judisa.dto.corte.CorteRequest;
import co.judisa.dto.corte.TotalesCortes;
import co.judisa.exception.BusinessRuleException;
import co.judisa.exception.FieldValidationException;
import co.judisa.exception.ResourceNotFoundException;
import co.judisa.mapper.CorteMapper;
import co.judisa.model.entity.Corte;
import co.judisa.model.entity.Empleado;
import co.judisa.model.entity.Referencia;
import co.judisa.model.enums.TipoTrabajo;
import co.judisa.repository.CorteRepository;
import co.judisa.repository.EmpleadoRepository;
import co.judisa.repository.ReferenciaRepository;
import co.judisa.service.impl.CorteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CorteServiceImplTest {

    @Mock
    CorteRepository corteRepository;
    @Mock
    EmpleadoRepository empleadoRepository;
    @Mock
    ReferenciaRepository referenciaRepository;

    CorteServiceImpl servicio;

    @BeforeEach
    void setUp() {
        servicio = new CorteServiceImpl(corteRepository, empleadoRepository, referenciaRepository, new CorteMapper());
    }

    private static Empleado empleado(long id, boolean activo) {
        Empleado e = new Empleado();
        e.setId(id);
        e.setNombre("Luis");
        e.setDocumento("1" + id);
        e.setTelefono("3000000000");
        e.setTipoTrabajo(TipoTrabajo.CORTADOR);
        e.setActivo(activo);
        return e;
    }

    private static Referencia referencia(long id) {
        Referencia r = new Referencia();
        r.setId(id);
        r.setCodigo("REF-" + id);
        r.setDescripcion("Jean");
        r.setValorUnitario(new BigDecimal("1800"));
        return r;
    }

    private static CorteRequest solicitud(long empleadoId) {
        return new CorteRequest(1L, empleadoId, 120, LocalDate.of(2026, 10, 1), "  ");
    }

    @Test
    void rf24_rechazaEmpleadoInactivo() {
        when(empleadoRepository.findById(5L)).thenReturn(Optional.of(empleado(5, false)));
        assertThatThrownBy(() -> servicio.crear(solicitud(5)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("inactivo");
    }

    @Test
    void rf24_rechazaEmpleadoInexistente() {
        when(empleadoRepository.findById(5L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> servicio.crear(solicitud(5))).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rf19_creaCorteYDescartaObservacionesVacias() {
        when(empleadoRepository.findById(2L)).thenReturn(Optional.of(empleado(2, true)));
        when(referenciaRepository.findById(1L)).thenReturn(Optional.of(referencia(1)));
        when(corteRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        var corte = servicio.crear(solicitud(2));
        assertThat(corte.cantidad()).isEqualTo(120);
        assertThat(corte.referenciaCodigo()).isEqualTo("REF-1");
        assertThat(corte.observaciones()).isNull();
    }

    @Test
    void rf20_editarConservaEmpleadoQueHoyEstaInactivo() {
        Corte existente = new Corte();
        existente.setId(10L);
        existente.setEmpleado(empleado(3, false));
        existente.setReferencia(referencia(1));
        when(corteRepository.findById(10L)).thenReturn(Optional.of(existente));
        when(corteRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(servicio.actualizar(10L, solicitud(3)).empleadoId()).isEqualTo(3L);
    }

    @Test
    void rf22_rangoDeFechasInvertidoEsInvalido() {
        var filtro = new CorteFiltro(null, null, LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 1));
        assertThatThrownBy(() -> servicio.listar(filtro, PageRequest.of(0, 10)))
                .isInstanceOf(FieldValidationException.class);
    }

    @Test
    void rf27_totalSinFechasUsaExtremos() {
        when(empleadoRepository.findById(2L)).thenReturn(Optional.of(empleado(2, true)));
        when(corteRepository.totalizar(eq(2L), any(), any())).thenReturn(new TotalesCortes(450L, 3L));
        var total = servicio.totalPiezas(2L, null, null);
        assertThat(total.totalPiezas()).isEqualTo(450);
        assertThat(total.totalCortes()).isEqualTo(3);
        assertThat(total.empleadoNombre()).isEqualTo("Luis");
    }
}

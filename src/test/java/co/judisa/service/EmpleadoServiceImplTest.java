package co.judisa.service;

import co.judisa.dto.empleado.EmpleadoRequest;
import co.judisa.exception.BusinessRuleException;
import co.judisa.exception.ResourceNotFoundException;
import co.judisa.mapper.CorteMapper;
import co.judisa.mapper.EmpleadoMapper;
import co.judisa.model.entity.Empleado;
import co.judisa.model.enums.TipoTrabajo;
import co.judisa.repository.CorteRepository;
import co.judisa.repository.EmpleadoRepository;
import co.judisa.repository.PagoRepository;
import co.judisa.service.impl.EmpleadoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmpleadoServiceImplTest {

    @Mock
    EmpleadoRepository empleadoRepository;
    @Mock
    CorteRepository corteRepository;
    @Mock
    PagoRepository pagoRepository;

    EmpleadoServiceImpl servicio;

    @BeforeEach
    void setUp() {
        servicio = new EmpleadoServiceImpl(empleadoRepository, corteRepository, pagoRepository,
                new EmpleadoMapper(), new CorteMapper());
    }

    private static Empleado empleado(long id) {
        Empleado e = new Empleado();
        e.setId(id);
        e.setNombre("Ana");
        e.setDocumento("123456");
        e.setTelefono("3001234567");
        e.setTipoTrabajo(TipoTrabajo.CORTADOR);
        return e;
    }

    @Test
    void rf12_noPermiteDocumentoDuplicadoAlCrear() {
        when(empleadoRepository.existsByDocumento("123456")).thenReturn(true);
        assertThatThrownBy(() -> servicio.crear(new EmpleadoRequest("Ana", " 123456 ", "3001234567", TipoTrabajo.OTRO)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("123456");
        verify(empleadoRepository, never()).saveAndFlush(any());
    }

    @Test
    void rf12_noPermiteDocumentoDeOtroEmpleadoAlActualizar() {
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleado(1)));
        when(empleadoRepository.existsByDocumentoAndIdNot("999999", 1L)).thenReturn(true);
        assertThatThrownBy(() -> servicio.actualizar(1L, new EmpleadoRequest("Ana", "999999", "3001234567", TipoTrabajo.OTRO)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void crearNormalizaEspaciosDelNombre() {
        when(empleadoRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        var creado = servicio.crear(new EmpleadoRequest("  María   Rojas ", "123456", "3001234567", TipoTrabajo.CORTADOR));
        assertThat(creado.nombre()).isEqualTo("María Rojas");
        assertThat(creado.tipoTrabajoNombre()).isEqualTo("Cortador");
    }

    @Test
    void rf14_noEliminaEmpleadoConCortesOPagos() {
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleado(1)));
        when(corteRepository.countByEmpleadoId(1L)).thenReturn(2L);
        when(pagoRepository.countByEmpleadoId(1L)).thenReturn(1L);
        assertThatThrownBy(() -> servicio.eliminar(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("El empleado tiene 2 cortes y 1 pago registrados. No se puede eliminar; puede desactivarlo.");
        verify(empleadoRepository, never()).delete(any(Empleado.class));
    }

    @Test
    void rf10_eliminaEmpleadoSinRegistros() {
        Empleado e = empleado(1);
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(e));
        servicio.eliminar(1L);
        verify(empleadoRepository).delete(e);
    }

    @Test
    void rf14_desactivarEsBorradoLogico() {
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleado(1)));
        when(empleadoRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(servicio.cambiarEstado(1L, false).activo()).isFalse();
    }

    @Test
    void empleadoInexistenteLanza404() {
        when(empleadoRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> servicio.obtener(9L)).isInstanceOf(ResourceNotFoundException.class);
    }
}

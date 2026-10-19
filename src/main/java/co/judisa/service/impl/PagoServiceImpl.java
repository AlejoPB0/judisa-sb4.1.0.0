package co.judisa.service.impl;

import co.judisa.dto.comun.PageResponse;
import co.judisa.dto.corte.TotalesCortes;
import co.judisa.dto.pago.CalculoPagoResponse;
import co.judisa.dto.pago.CambioEstadoPagoRequest;
import co.judisa.dto.pago.PagoFiltro;
import co.judisa.dto.pago.PagoRequest;
import co.judisa.dto.pago.PagoResponse;
import co.judisa.dto.pago.SaldoPendienteResponse;
import co.judisa.dto.pago.TotalPagosResponse;
import co.judisa.dto.pago.TotalesPagos;
import co.judisa.exception.BusinessRuleException;
import co.judisa.exception.FieldValidationException;
import co.judisa.exception.ResourceNotFoundException;
import co.judisa.mapper.PagoMapper;
import co.judisa.model.entity.Empleado;
import co.judisa.model.entity.Pago;
import co.judisa.model.enums.EstadoPago;
import co.judisa.repository.CorteRepository;
import co.judisa.repository.EmpleadoRepository;
import co.judisa.repository.PagoRepository;
import co.judisa.service.PagoService;
import co.judisa.util.Formatos;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class PagoServiceImpl implements PagoService {

    private static final LocalDate FECHA_MINIMA = LocalDate.of(1900, 1, 1);
    private static final LocalDate FECHA_MAXIMA = LocalDate.of(9999, 12, 31);

    private final PagoRepository pagoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final CorteRepository corteRepository;
    private final PagoMapper mapper;

    public PagoServiceImpl(PagoRepository pagoRepository, EmpleadoRepository empleadoRepository,
                           CorteRepository corteRepository, PagoMapper mapper) {
        this.pagoRepository = pagoRepository;
        this.empleadoRepository = empleadoRepository;
        this.corteRepository = corteRepository;
        this.mapper = mapper;
    }

    /** RF-33: filtros por empleado, estado y rango de fechas. */
    @Override
    public PageResponse<PagoResponse> listar(PagoFiltro filtro, Pageable pageable) {
        validarRango(filtro.desde(), filtro.hasta());
        return PageResponse.of(pagoRepository.findAll(PagoRepository.conFiltro(filtro), pageable), mapper::toResponse);
    }

    @Override
    public PagoResponse obtener(Long id) {
        return mapper.toResponse(buscar(id));
    }

    @Override
    public PagoRequest obtenerParaEditar(Long id) {
        return mapper.toRequest(buscar(id));
    }

    /** RF-30 registrar con total calculado en el servidor; RF-34 valores positivos (DTO); RF-35 empleado obligatorio; RF-38 estado. */
    @Override
    @Transactional
    public PagoResponse crear(PagoRequest request) {
        Pago pago = new Pago();
        pago.setEmpleado(empleadoAsignable(request.getEmpleadoId(), null));
        copiarDatos(pago, request);
        return mapper.toResponse(pagoRepository.saveAndFlush(pago));
    }

    /** RF-31 modificar: el total se recalcula siempre. */
    @Override
    @Transactional
    public PagoResponse actualizar(Long id, PagoRequest request) {
        Pago pago = buscar(id);
        pago.setEmpleado(empleadoAsignable(request.getEmpleadoId(), pago.getEmpleado()));
        copiarDatos(pago, request);
        return mapper.toResponse(pagoRepository.saveAndFlush(pago));
    }

    /** RF-32 eliminar. */
    @Override
    @Transactional
    public void eliminar(Long id) {
        pagoRepository.delete(buscar(id));
    }

    /** RF-30, RF-36: total = piezas × valor unitario. */
    @Override
    public CalculoPagoResponse calcular(Integer piezas, BigDecimal valorUnitario) {
        return new CalculoPagoResponse(piezas, valorUnitario, calcularTotal(piezas, valorUnitario));
    }

    /** RF-38: marcar como pendiente o realizado. Si se marca realizado sin fecha, se usa la de hoy. */
    @Override
    @Transactional
    public PagoResponse cambiarEstado(Long id, CambioEstadoPagoRequest request) {
        Pago pago = buscar(id);
        pago.setEstado(request.estado());
        if (request.estado() == EstadoPago.REALIZADO) {
            pago.setFechaPagoEfectivo(Objects.requireNonNullElse(request.fechaPagoEfectivo(), Formatos.hoy()));
        } else {
            pago.setFechaPagoEfectivo(null);
        }
        return mapper.toResponse(pagoRepository.saveAndFlush(pago));
    }

    /** RF-37: total pagado (REALIZADO) y pendiente a un empleado (o a todos) en un periodo. */
    @Override
    public TotalPagosResponse total(Long empleadoId, LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        String nombre = empleadoId == null ? null : buscarEmpleado(empleadoId).getNombre();
        TotalesPagos t = pagoRepository.totalizar(empleadoId, minimo(desde), maximo(hasta));
        return new TotalPagosResponse(empleadoId, nombre, desde, hasta, t.totalPagado(), t.piezasPagadas(),
                t.totalPendiente(), t.piezasPendientes(), t.cantidad());
    }

    /** RF-39: piezas cortadas frente a piezas pagadas y saldo en pesos. */
    @Override
    public SaldoPendienteResponse saldoPendiente(Long empleadoId, LocalDate desde, LocalDate hasta) {
        if (empleadoId == null) {
            throw new FieldValidationException("empleadoId", "Seleccione el empleado");
        }
        validarRango(desde, hasta);
        Empleado empleado = buscarEmpleado(empleadoId);
        LocalDate d = minimo(desde);
        LocalDate h = maximo(hasta);
        TotalesCortes cortes = corteRepository.totalizar(empleadoId, d, h);
        BigDecimal producido = Objects.requireNonNullElse(corteRepository.valorProducido(empleadoId, d, h), BigDecimal.ZERO);
        TotalesPagos pagos = pagoRepository.totalizar(empleadoId, d, h);
        return new SaldoPendienteResponse(empleadoId, empleado.getNombre(), desde, hasta,
                cortes.piezas(), producido,
                pagos.piezasPagadas(), pagos.totalPagado(),
                pagos.piezasPendientes(), pagos.totalPendiente(),
                cortes.piezas() - pagos.piezasPagadas(),
                producido.subtract(pagos.totalPagado()));
    }

    /** Total en pesos enteros; los dos factores ya vienen validados como positivos. */
    public static BigDecimal calcularTotal(Integer piezas, BigDecimal valorUnitario) {
        return valorUnitario.multiply(BigDecimal.valueOf(piezas.longValue())).setScale(0, java.math.RoundingMode.HALF_UP);
    }

    private void copiarDatos(Pago pago, PagoRequest request) {
        pago.setPiezas(request.getPiezas());
        pago.setValorUnitario(request.getValorUnitario());
        pago.setTotal(calcularTotal(request.getPiezas(), request.getValorUnitario()));
        String nota = request.getNota();
        pago.setNota(nota == null || nota.isBlank() ? null : nota.trim());
        pago.setFecha(request.getFecha());
        EstadoPago estado = Objects.requireNonNullElse(request.getEstado(), EstadoPago.PENDIENTE);
        pago.setEstado(estado);
        if (estado == EstadoPago.REALIZADO) {
            LocalDate efectivo = Objects.requireNonNullElse(request.getFechaPagoEfectivo(), request.getFecha());
            if (efectivo.isBefore(request.getFecha())) {
                throw new FieldValidationException("fechaPagoEfectivo",
                        "La fecha de pago no puede ser anterior a la fecha del registro");
            }
            pago.setFechaPagoEfectivo(efectivo);
        } else {
            pago.setFechaPagoEfectivo(null);
        }
    }

    /** RF-30: el empleado debe existir y estar activo para un pago nuevo (al editar se conserva aunque esté inactivo). */
    private Empleado empleadoAsignable(Long empleadoId, Empleado actual) {
        if (actual != null && actual.getId().equals(empleadoId)) {
            return actual;
        }
        Empleado empleado = buscarEmpleado(empleadoId);
        if (!empleado.isActivo()) {
            throw new BusinessRuleException("El empleado " + empleado.getNombre()
                    + " está inactivo. Actívelo antes de registrarle pagos.");
        }
        return empleado;
    }

    private Empleado buscarEmpleado(Long id) {
        return empleadoRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.de("El empleado", id));
    }

    private static LocalDate minimo(LocalDate desde) {
        return Objects.requireNonNullElse(desde, FECHA_MINIMA);
    }

    private static LocalDate maximo(LocalDate hasta) {
        return Objects.requireNonNullElse(hasta, FECHA_MAXIMA);
    }

    private static void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new FieldValidationException("hasta", "La fecha final no puede ser anterior a la inicial");
        }
    }

    private Pago buscar(Long id) {
        return pagoRepository.findWithEmpleadoById(id).orElseThrow(() -> ResourceNotFoundException.de("El pago", id));
    }
}

package co.judisa.service.impl;

import co.judisa.dto.corte.CorteRequest;
import co.judisa.dto.empleado.EmpleadoRequest;
import co.judisa.dto.pago.PagoRequest;
import co.judisa.dto.referencia.ReferenciaRequest;
import co.judisa.dto.sync.OperacionSyncRequest;
import co.judisa.dto.sync.ResultadoSync;
import co.judisa.dto.sync.SyncRequest;
import co.judisa.dto.sync.SyncResponse;
import co.judisa.exception.BusinessRuleException;
import co.judisa.exception.FieldValidationException;
import co.judisa.exception.ResourceNotFoundException;
import co.judisa.model.entity.EntidadSincronizable;
import co.judisa.model.entity.SyncOperacion;
import co.judisa.model.enums.EntidadSync;
import co.judisa.model.enums.EstadoSync;
import co.judisa.repository.CorteRepository;
import co.judisa.repository.EmpleadoRepository;
import co.judisa.repository.PagoRepository;
import co.judisa.repository.ReferenciaRepository;
import co.judisa.repository.SyncOperacionRepository;
import co.judisa.service.CorteService;
import co.judisa.service.EmpleadoService;
import co.judisa.service.PagoService;
import co.judisa.service.ReferenciaService;
import co.judisa.service.SyncService;
import co.judisa.util.Formatos;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * RF-57: sincronización de la cola offline. Cada operación corre en su propia transacción: si una falla,
 * se deshace solo esa y las demás siguen. La tabla {@code sync_operaciones} guarda el resultado por clientId,
 * así un reintento no duplica registros. Ante cambios concurrentes gana el último cambio (por fecha).
 */
@Service
public class SyncServiceImpl implements SyncService {

    private static final Logger log = LoggerFactory.getLogger(SyncServiceImpl.class);

    private final EmpleadoService empleadoService;
    private final ReferenciaService referenciaService;
    private final CorteService corteService;
    private final PagoService pagoService;
    private final EmpleadoRepository empleadoRepository;
    private final ReferenciaRepository referenciaRepository;
    private final CorteRepository corteRepository;
    private final PagoRepository pagoRepository;
    private final SyncOperacionRepository syncRepository;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final TransactionTemplate tx;

    public SyncServiceImpl(EmpleadoService empleadoService, ReferenciaService referenciaService,
                           CorteService corteService, PagoService pagoService,
                           EmpleadoRepository empleadoRepository, ReferenciaRepository referenciaRepository,
                           CorteRepository corteRepository, PagoRepository pagoRepository,
                           SyncOperacionRepository syncRepository, ObjectMapper objectMapper, Validator validator,
                           PlatformTransactionManager transactionManager) {
        this.empleadoService = empleadoService;
        this.referenciaService = referenciaService;
        this.corteService = corteService;
        this.pagoService = pagoService;
        this.empleadoRepository = empleadoRepository;
        this.referenciaRepository = referenciaRepository;
        this.corteRepository = corteRepository;
        this.pagoRepository = pagoRepository;
        this.syncRepository = syncRepository;
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.tx = new TransactionTemplate(transactionManager);
        this.tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public SyncResponse sincronizar(SyncRequest request) {
        List<ResultadoSync> resultados = new ArrayList<>();
        for (OperacionSyncRequest op : request.operaciones()) {
            resultados.add(procesarAislada(op));
        }
        return new SyncResponse(OffsetDateTime.now(Formatos.ZONA), resultados);
    }

    private ResultadoSync procesarAislada(OperacionSyncRequest op) {
        try {
            return tx.execute(estado -> procesar(op));
        } catch (BusinessRuleException | FieldValidationException | ResourceNotFoundException e) {
            return rechazar(op, e.getMessage());
        } catch (DataIntegrityViolationException e) {
            // Otra operación paralela con el mismo clientId, o un dato duplicado
            Optional<SyncOperacion> previa = syncRepository.findById(op.clientId());
            if (previa.isPresent()) {
                return repetida(previa.get());
            }
            return rechazar(op, "La operación entra en conflicto con datos existentes (registro duplicado o en uso)");
        } catch (RuntimeException e) {
            log.error("Error inesperado sincronizando la operación {}", op.clientId(), e);
            return new ResultadoSync(op.clientId(), EstadoSync.RECHAZADA, op.id(),
                    "Error inesperado en el servidor. Intente de nuevo.", null);
        }
    }

    private ResultadoSync procesar(OperacionSyncRequest op) {
        Optional<SyncOperacion> previa = syncRepository.findById(op.clientId());
        if (previa.isPresent()) {
            return repetida(previa.get());
        }
        return switch (op.operacion()) {
            case CREAR -> crear(op);
            case ACTUALIZAR -> actualizar(op);
            case ELIMINAR -> eliminar(op);
        };
    }

    private ResultadoSync crear(OperacionSyncRequest op) {
        Object request = convertir(op);
        Long id = switch (op.entidad()) {
            case EMPLEADO -> empleadoService.crear((EmpleadoRequest) request).id();
            case REFERENCIA -> referenciaService.crear((ReferenciaRequest) request).id();
            case CORTE -> corteService.crear((CorteRequest) request).id();
            case PAGO -> pagoService.crear((PagoRequest) request).id();
        };
        buscarEntidad(op.entidad(), id).ifPresent(e -> e.setClientId(op.clientId()));
        return aplicada(op, id);
    }

    private ResultadoSync actualizar(OperacionSyncRequest op) {
        Long id = resolverId(op);
        Optional<? extends EntidadSincronizable> actual = id == null ? Optional.empty() : buscarEntidad(op.entidad(), id);
        if (actual.isEmpty()) {
            throw new BusinessRuleException("El registro que intenta modificar ya no existe en el servidor.");
        }
        if (hayConflicto(actual.get(), op)) {
            return conflicto(op, id);
        }
        Object request = convertir(op);
        switch (op.entidad()) {
            case EMPLEADO -> empleadoService.actualizar(id, (EmpleadoRequest) request);
            case REFERENCIA -> referenciaService.actualizar(id, (ReferenciaRequest) request);
            case CORTE -> corteService.actualizar(id, (CorteRequest) request);
            case PAGO -> pagoService.actualizar(id, (PagoRequest) request);
        }
        return aplicada(op, id);
    }

    /** Eliminar algo que ya no existe se considera aplicado: el resultado final es el mismo. */
    private ResultadoSync eliminar(OperacionSyncRequest op) {
        Long id = resolverId(op);
        Optional<? extends EntidadSincronizable> actual = id == null ? Optional.empty() : buscarEntidad(op.entidad(), id);
        if (actual.isEmpty()) {
            registrar(op, EstadoSync.APLICADA, id, null);
            return new ResultadoSync(op.clientId(), EstadoSync.APLICADA, id, null, null);
        }
        if (hayConflicto(actual.get(), op)) {
            return conflicto(op, id);
        }
        switch (op.entidad()) {
            case EMPLEADO -> empleadoService.eliminar(id);
            case REFERENCIA -> referenciaService.eliminar(id);
            case CORTE -> corteService.eliminar(id);
            case PAGO -> pagoService.eliminar(id);
        }
        registrar(op, EstadoSync.APLICADA, id, null);
        return new ResultadoSync(op.clientId(), EstadoSync.APLICADA, id, null, null);
    }

    /** Gana el último cambio: si el servidor se modificó después del cambio hecho en el teléfono, no se aplica. */
    private static boolean hayConflicto(EntidadSincronizable entidad, OperacionSyncRequest op) {
        return op.modificadoEn() != null && entidad.getUpdatedAt() != null
                && entidad.getUpdatedAt().isAfter(op.modificadoEn());
    }

    private Long resolverId(OperacionSyncRequest op) {
        if (op.id() != null) {
            return op.id();
        }
        if (op.idClientId() == null || op.idClientId().isBlank()) {
            throw new FieldValidationException("id", "Indique id o idClientId del registro");
        }
        return buscarPorClientId(op.entidad(), op.idClientId()).map(EntidadSincronizable::getId).orElse(null);
    }

    /** Convierte {@code datos} al Request de la entidad, resuelve referencias por clientId y valida. */
    private Object convertir(OperacionSyncRequest op) {
        if (op.datos() == null || !op.datos().isObject()) {
            throw new FieldValidationException("datos", "La operación no trae datos");
        }
        ObjectNode datos = (ObjectNode) op.datos().deepCopy();
        if (op.entidad() == EntidadSync.CORTE || op.entidad() == EntidadSync.PAGO) {
            resolverReferencia(datos, "empleadoId", "empleadoClientId", EntidadSync.EMPLEADO, "El empleado");
        }
        if (op.entidad() == EntidadSync.CORTE) {
            resolverReferencia(datos, "referenciaId", "referenciaClientId", EntidadSync.REFERENCIA, "La referencia");
        }
        Class<?> tipo = switch (op.entidad()) {
            case EMPLEADO -> EmpleadoRequest.class;
            case REFERENCIA -> ReferenciaRequest.class;
            case CORTE -> CorteRequest.class;
            case PAGO -> PagoRequest.class;
        };
        Object request;
        try {
            request = objectMapper.treeToValue(datos, tipo);
        } catch (JacksonException | IllegalArgumentException e) {
            throw new FieldValidationException("datos",
                    "Los datos no tienen un formato válido. Revise números, fechas (yyyy-MM-dd) y valores permitidos.");
        }
        Set<ConstraintViolation<Object>> errores = validator.validate(request);
        if (!errores.isEmpty()) {
            String mensaje = errores.stream()
                    .sorted(Comparator.comparing(v -> v.getPropertyPath().toString()))
                    .map(ConstraintViolation::getMessage)
                    .distinct()
                    .collect(Collectors.joining(". "));
            throw new FieldValidationException(errores.iterator().next().getPropertyPath().toString(), mensaje);
        }
        return request;
    }

    private void resolverReferencia(ObjectNode datos, String campoId, String campoClientId, EntidadSync entidad,
                                    String nombre) {
        JsonNode clientId = datos.remove(campoClientId);
        if (datos.hasNonNull(campoId) || clientId == null || clientId.isNull()) {
            return;
        }
        Long id = buscarPorClientId(entidad, clientId.asString()).map(EntidadSincronizable::getId)
                .orElseThrow(() -> new BusinessRuleException(nombre
                        + " creado sin conexión no llegó al servidor; sincronice primero esa operación."));
        datos.put(campoId, id);
    }

    private Optional<? extends EntidadSincronizable> buscarEntidad(EntidadSync entidad, Long id) {
        return switch (entidad) {
            case EMPLEADO -> empleadoRepository.findById(id);
            case REFERENCIA -> referenciaRepository.findById(id);
            case CORTE -> corteRepository.findById(id);
            case PAGO -> pagoRepository.findById(id);
        };
    }

    private Optional<? extends EntidadSincronizable> buscarPorClientId(EntidadSync entidad, String clientId) {
        return switch (entidad) {
            case EMPLEADO -> empleadoRepository.findByClientId(clientId);
            case REFERENCIA -> referenciaRepository.findByClientId(clientId);
            case CORTE -> corteRepository.findByClientId(clientId);
            case PAGO -> pagoRepository.findByClientId(clientId);
        };
    }

    /** Versión actual del registro en el servidor, con el mismo formato que la API (null si no existe). */
    private Object registro(EntidadSync entidad, Long id) {
        if (id == null) {
            return null;
        }
        try {
            return switch (entidad) {
                case EMPLEADO -> empleadoService.obtener(id);
                case REFERENCIA -> referenciaService.obtener(id);
                case CORTE -> corteService.obtener(id);
                case PAGO -> pagoService.obtener(id);
            };
        } catch (ResourceNotFoundException e) {
            return null;
        }
    }

    private ResultadoSync aplicada(OperacionSyncRequest op, Long id) {
        registrar(op, EstadoSync.APLICADA, id, null);
        return new ResultadoSync(op.clientId(), EstadoSync.APLICADA, id, null, registro(op.entidad(), id));
    }

    private ResultadoSync conflicto(OperacionSyncRequest op, Long id) {
        String mensaje = "Se conservó un cambio más reciente hecho en otro dispositivo.";
        registrar(op, EstadoSync.CONFLICTO, id, mensaje);
        return new ResultadoSync(op.clientId(), EstadoSync.CONFLICTO, id, mensaje, registro(op.entidad(), id));
    }

    /** Un reintento devuelve lo mismo que la primera vez; si se aplicó, se informa YA_PROCESADA. */
    private ResultadoSync repetida(SyncOperacion previa) {
        EstadoSync estado = previa.getEstado() == EstadoSync.APLICADA ? EstadoSync.YA_PROCESADA : previa.getEstado();
        Object registro = tx.execute(s -> registro(previa.getEntidad(), previa.getEntidadId()));
        return new ResultadoSync(previa.getClientId(), estado, previa.getEntidadId(), previa.getMensaje(), registro);
    }

    /** Se guarda el rechazo en una transacción aparte, porque la de la operación ya se deshizo. */
    private ResultadoSync rechazar(OperacionSyncRequest op, String mensaje) {
        String texto = mensaje == null ? "La operación no es válida" : mensaje;
        try {
            tx.executeWithoutResult(s -> {
                if (!syncRepository.existsById(op.clientId())) {
                    registrar(op, EstadoSync.RECHAZADA, op.id(), texto);
                }
            });
        } catch (DataIntegrityViolationException e) {
            log.debug("La operación {} ya estaba registrada", op.clientId());
        }
        return new ResultadoSync(op.clientId(), EstadoSync.RECHAZADA, op.id(), texto, null);
    }

    private void registrar(OperacionSyncRequest op, EstadoSync estado, Long id, String mensaje) {
        String texto = mensaje != null && mensaje.length() > 500 ? mensaje.substring(0, 500) : mensaje;
        syncRepository.saveAndFlush(new SyncOperacion(op.clientId(), op.entidad(), op.operacion(), estado, id, texto));
    }

}

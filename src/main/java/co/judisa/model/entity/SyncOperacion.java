package co.judisa.model.entity;

import co.judisa.model.enums.EntidadSync;
import co.judisa.model.enums.EstadoSync;
import co.judisa.model.enums.OperacionSync;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/** Registro de operaciones de sincronización ya procesadas: garantiza la idempotencia por clientId. */
@Entity
@Table(name = "sync_operaciones")
public class SyncOperacion {

    @Id
    @Column(name = "client_id", length = 36)
    private String clientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EntidadSync entidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OperacionSync operacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSync estado;

    @Column(name = "entidad_id")
    private Long entidadId;

    @Column(length = 500)
    private String mensaje;

    @CreationTimestamp
    @Column(name = "procesada_en", nullable = false, updatable = false)
    private OffsetDateTime procesadaEn;

    protected SyncOperacion() {
    }

    public SyncOperacion(String clientId, EntidadSync entidad, OperacionSync operacion, EstadoSync estado,
                         Long entidadId, String mensaje) {
        this.clientId = clientId;
        this.entidad = entidad;
        this.operacion = operacion;
        this.estado = estado;
        this.entidadId = entidadId;
        this.mensaje = mensaje;
    }

    public String getClientId() {
        return clientId;
    }

    public EntidadSync getEntidad() {
        return entidad;
    }

    public OperacionSync getOperacion() {
        return operacion;
    }

    public EstadoSync getEstado() {
        return estado;
    }

    public Long getEntidadId() {
        return entidadId;
    }

    public String getMensaje() {
        return mensaje;
    }
}

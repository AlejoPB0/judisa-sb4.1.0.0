package co.judisa.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

/**
 * RF-57: entidades que las apps pueden crear sin conexión. {@code clientId} es el UUID que generó el teléfono
 * en la operación CREAR; permite que operaciones posteriores apunten al registro antes de conocer su id.
 */
@MappedSuperclass
public abstract class EntidadSincronizable extends EntidadAuditable {

    @Column(name = "client_id", unique = true, length = 36)
    private String clientId;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}

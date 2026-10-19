package co.judisa.model.entity;

import co.judisa.model.enums.TipoTrabajo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/** Trabajador del taller. Se relaciona 1:N con {@link Corte} y {@link Pago} (navegación desde el lado N). */
@Entity
@Table(name = "empleados")
public class Empleado extends EntidadSincronizable {

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, unique = true, length = 20)
    private String documento;

    @Column(nullable = false, length = 15)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_trabajo", nullable = false, length = 20)
    private TipoTrabajo tipoTrabajo;

    @Column(nullable = false)
    private boolean activo = true;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public TipoTrabajo getTipoTrabajo() {
        return tipoTrabajo;
    }

    public void setTipoTrabajo(TipoTrabajo tipoTrabajo) {
        this.tipoTrabajo = tipoTrabajo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}

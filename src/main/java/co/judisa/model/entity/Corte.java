package co.judisa.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

/** Corte de producción: cuántas piezas de una referencia cortó un empleado en una fecha. */
@Entity
@Table(name = "cortes", indexes = {
        @Index(name = "idx_cortes_fecha", columnList = "fecha"),
        @Index(name = "idx_cortes_empleado", columnList = "empleado_id"),
        @Index(name = "idx_cortes_referencia", columnList = "referencia_id")
})
public class Corte extends EntidadSincronizable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "referencia_id", nullable = false)
    private Referencia referencia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(length = 500)
    private String observaciones;

    public Referencia getReferencia() {
        return referencia;
    }

    public void setReferencia(Referencia referencia) {
        this.referencia = referencia;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}

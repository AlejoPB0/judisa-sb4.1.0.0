package co.judisa.model.entity;

import co.judisa.model.enums.EstadoPago;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Pago a un empleado. En el Sprint 1 solo se consulta (detalle del empleado y regla RF-14);
 * el registro y cálculo del total se implementan en el Sprint 3.
 */
@Entity
@Table(name = "pagos", indexes = {
        @Index(name = "idx_pagos_fecha", columnList = "fecha"),
        @Index(name = "idx_pagos_empleado", columnList = "empleado_id"),
        @Index(name = "idx_pagos_estado", columnList = "estado")
})
public class Pago extends EntidadSincronizable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @Column(nullable = false)
    private Integer piezas;

    @Column(name = "valor_unitario", nullable = false, precision = 14, scale = 0)
    private BigDecimal valorUnitario;

    @Column(nullable = false, precision = 16, scale = 0)
    private BigDecimal total;

    @Column(length = 300)
    private String nota;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPago estado = EstadoPago.PENDIENTE;

    @Column(name = "fecha_pago_efectivo")
    private LocalDate fechaPagoEfectivo;

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public Integer getPiezas() {
        return piezas;
    }

    public void setPiezas(Integer piezas) {
        this.piezas = piezas;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public void setEstado(EstadoPago estado) {
        this.estado = estado;
    }

    public LocalDate getFechaPagoEfectivo() {
        return fechaPagoEfectivo;
    }

    public void setFechaPagoEfectivo(LocalDate fechaPagoEfectivo) {
        this.fechaPagoEfectivo = fechaPagoEfectivo;
    }
}

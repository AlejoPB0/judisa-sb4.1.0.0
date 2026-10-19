package co.judisa.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/** Referencia (modelo de prenda) con su valor unitario sugerido en COP. */
@Entity
@Table(name = "referencias")
public class Referencia extends EntidadSincronizable {

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(nullable = false, length = 200)
    private String descripcion;

    @Column(name = "valor_unitario", nullable = false, precision = 14, scale = 0)
    private BigDecimal valorUnitario;

    @Column(nullable = false)
    private boolean activa = true;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}

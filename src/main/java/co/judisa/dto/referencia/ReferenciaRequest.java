package co.judisa.dto.referencia;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ReferenciaRequest {

    @NotBlank(message = "El código es obligatorio")
    @Size(min = 2, max = 30, message = "El código debe tener entre 2 y 30 caracteres")
    private String codigo;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 200, message = "La descripción admite máximo 200 caracteres")
    private String descripcion;

    @NotNull(message = "El valor unitario es obligatorio")
    @Positive(message = "El valor unitario debe ser mayor que cero")
    @Digits(integer = 12, fraction = 0, message = "El valor unitario debe ser un número entero de pesos")
    private BigDecimal valorUnitario;

    public ReferenciaRequest() {
    }

    public ReferenciaRequest(String codigo, String descripcion, BigDecimal valorUnitario) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.valorUnitario = valorUnitario;
    }

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
}

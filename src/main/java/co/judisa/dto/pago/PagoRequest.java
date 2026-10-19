package co.judisa.dto.pago;

import co.judisa.model.enums.EstadoPago;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * RF-30, RF-34, RF-35: datos de un pago. No incluye el total: el servidor lo calcula (piezas × valor unitario)
 * y cualquier "total" que envíe el cliente se ignora.
 */
public class PagoRequest {

    @NotNull(message = "Seleccione el empleado")
    private Long empleadoId;

    @NotNull(message = "Las piezas son obligatorias")
    @Positive(message = "Las piezas deben ser un número entero mayor que cero")
    @Max(value = 1_000_000, message = "Las piezas no pueden superar 1.000.000")
    private Integer piezas;

    @NotNull(message = "El valor unitario es obligatorio")
    @Positive(message = "El valor unitario debe ser mayor que cero")
    @Digits(integer = 12, fraction = 0, message = "El valor unitario debe ser un número entero de pesos")
    private BigDecimal valorUnitario;

    @Size(max = 300, message = "La nota admite máximo 300 caracteres")
    private String nota;

    @NotNull(message = "La fecha es obligatoria")
    @PastOrPresent(message = "La fecha no puede ser futura")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    private EstadoPago estado;

    @PastOrPresent(message = "La fecha de pago no puede ser futura")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaPagoEfectivo;

    public PagoRequest() {
    }

    public PagoRequest(Long empleadoId, Integer piezas, BigDecimal valorUnitario, String nota, LocalDate fecha,
                       EstadoPago estado, LocalDate fechaPagoEfectivo) {
        this.empleadoId = empleadoId;
        this.piezas = piezas;
        this.valorUnitario = valorUnitario;
        this.nota = nota;
        this.fecha = fecha;
        this.estado = estado;
        this.fechaPagoEfectivo = fechaPagoEfectivo;
    }

    public Long getEmpleadoId() {
        return empleadoId;
    }

    public void setEmpleadoId(Long empleadoId) {
        this.empleadoId = empleadoId;
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

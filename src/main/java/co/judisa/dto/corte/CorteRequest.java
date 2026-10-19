package co.judisa.dto.corte;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** RF-19, RF-23, RF-28: datos de un corte de producción. */
public class CorteRequest {

    @NotNull(message = "Seleccione la referencia")
    private Long referenciaId;

    @NotNull(message = "Seleccione el empleado responsable")
    private Long empleadoId;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser un número entero mayor que cero")
    @Max(value = 100000, message = "La cantidad no puede superar 100.000 piezas")
    private Integer cantidad;

    @NotNull(message = "La fecha es obligatoria")
    @PastOrPresent(message = "La fecha no puede ser futura")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @Size(max = 500, message = "Las observaciones admiten máximo 500 caracteres")
    private String observaciones;

    public CorteRequest() {
    }

    public CorteRequest(Long referenciaId, Long empleadoId, Integer cantidad, LocalDate fecha, String observaciones) {
        this.referenciaId = referenciaId;
        this.empleadoId = empleadoId;
        this.cantidad = cantidad;
        this.fecha = fecha;
        this.observaciones = observaciones;
    }

    public Long getReferenciaId() {
        return referenciaId;
    }

    public void setReferenciaId(Long referenciaId) {
        this.referenciaId = referenciaId;
    }

    public Long getEmpleadoId() {
        return empleadoId;
    }

    public void setEmpleadoId(Long empleadoId) {
        this.empleadoId = empleadoId;
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

package co.judisa.dto.empleado;

import co.judisa.model.enums.TipoTrabajo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** RF-8, RF-13: datos obligatorios para registrar o modificar un empleado. */
public class EmpleadoRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 120, message = "El nombre debe tener entre 2 y 120 caracteres")
    private String nombre;

    @NotBlank(message = "El documento es obligatorio")
    @Pattern(regexp = "\\d{5,20}", message = "El documento debe tener entre 5 y 20 dígitos, sin puntos ni espacios")
    private String documento;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "\\d{7,15}", message = "El teléfono debe tener entre 7 y 15 dígitos")
    private String telefono;

    @NotNull(message = "Seleccione el tipo de trabajo")
    private TipoTrabajo tipoTrabajo;

    public EmpleadoRequest() {
    }

    public EmpleadoRequest(String nombre, String documento, String telefono, TipoTrabajo tipoTrabajo) {
        this.nombre = nombre;
        this.documento = documento;
        this.telefono = telefono;
        this.tipoTrabajo = tipoTrabajo;
    }

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
}

package co.judisa.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** RF-6: contraseña actual, nueva (mínimo 8 caracteres) y confirmación. Clase mutable para el formulario web. */
public class CambioPasswordRequest {

    @NotBlank(message = "Ingrese su contraseña actual")
    private String passwordActual;

    @NotBlank(message = "Ingrese la nueva contraseña")
    @Size(min = 8, max = 72, message = "La nueva contraseña debe tener entre 8 y 72 caracteres")
    private String passwordNueva;

    @NotBlank(message = "Confirme la nueva contraseña")
    private String confirmacion;

    public CambioPasswordRequest() {
    }

    public CambioPasswordRequest(String passwordActual, String passwordNueva, String confirmacion) {
        this.passwordActual = passwordActual;
        this.passwordNueva = passwordNueva;
        this.confirmacion = confirmacion;
    }

    public String getPasswordActual() {
        return passwordActual;
    }

    public void setPasswordActual(String passwordActual) {
        this.passwordActual = passwordActual;
    }

    public String getPasswordNueva() {
        return passwordNueva;
    }

    public void setPasswordNueva(String passwordNueva) {
        this.passwordNueva = passwordNueva;
    }

    public String getConfirmacion() {
        return confirmacion;
    }

    public void setConfirmacion(String confirmacion) {
        this.confirmacion = confirmacion;
    }
}

package co.judisa.exception;

import java.util.Map;

/** Error de validación detectado en el servicio y asociado a un campo concreto (HTTP 400). */
public class FieldValidationException extends RuntimeException {

    private final transient Map<String, String> fields;

    public FieldValidationException(String campo, String mensaje) {
        super(mensaje);
        this.fields = Map.of(campo, mensaje);
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public String getCampo() {
        return fields.keySet().iterator().next();
    }
}

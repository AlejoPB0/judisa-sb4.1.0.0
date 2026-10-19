package co.judisa.exception;

/** El recurso solicitado no existe (HTTP 404). */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException de(String recurso, Long id) {
        return new ResourceNotFoundException(recurso + " con id " + id + " no existe");
    }
}

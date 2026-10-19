package co.judisa.exception;

/** Token de refresco inválido, vencido o revocado (HTTP 401). */
public class SesionInvalidaException extends RuntimeException {

    public SesionInvalidaException() {
        super("La sesión venció o no es válida. Inicie sesión de nuevo.");
    }
}

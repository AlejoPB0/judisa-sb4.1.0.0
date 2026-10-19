package co.judisa.exception;

/** Se incumple una regla de negocio, por ejemplo un documento duplicado (HTTP 409). */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}

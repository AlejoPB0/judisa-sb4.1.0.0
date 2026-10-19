package co.judisa.exception;

import co.judisa.dto.comun.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Manejo centralizado de errores de la API REST, con mensajes en español y formato uniforme. */
@RestControllerAdvice(basePackages = "co.judisa.controller.api")
public class ApiExceptionHandler {

    public static final String MENSAJE_CREDENCIALES = "Usuario o contraseña incorrectos";
    public static final String MENSAJE_BLOQUEO =
            "Cuenta bloqueada temporalmente por intentos fallidos. Intente de nuevo en 15 minutos.";

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        ex.getBindingResult().getGlobalErrors()
                .forEach(e -> campos.putIfAbsent(e.getObjectName(), e.getDefaultMessage()));
        return respuesta(HttpStatus.BAD_REQUEST, "Hay campos con errores", campos);
    }

    @ExceptionHandler(FieldValidationException.class)
    public ResponseEntity<ErrorResponse> validacionServicio(FieldValidationException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getFields());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> restricciones(ConstraintViolationException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v -> {
            String ruta = v.getPropertyPath().toString();
            campos.putIfAbsent(ruta.substring(ruta.lastIndexOf('.') + 1), v.getMessage());
        });
        return respuesta(HttpStatus.BAD_REQUEST, "Hay campos con errores", campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> jsonInvalido(HttpMessageNotReadableException ex) {
        return respuesta(HttpStatus.BAD_REQUEST,
                "La petición no tiene un formato válido. Revise números, fechas (yyyy-MM-dd) y valores permitidos.", null);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> parametroInvalido(Exception ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "Parámetro inválido o faltante en la petición", null);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(ResourceNotFoundException ex) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> rutaNoExiste(NoResourceFoundException ex) {
        return respuesta(HttpStatus.NOT_FOUND, "La ruta solicitada no existe", null);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> reglaNegocio(BusinessRuleException ex) {
        return respuesta(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    /** Respaldo por si dos peticiones simultáneas pasan la validación del servicio (p. ej. documento duplicado). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integridad(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad: {}", ex.getMostSpecificCause().getMessage());
        return respuesta(HttpStatus.CONFLICT,
                "La operación entra en conflicto con datos existentes (registro duplicado o en uso)", null);
    }

    /** RF-5: mensaje genérico, sin indicar si falló el usuario o la contraseña. */
    @ExceptionHandler({BadCredentialsException.class, DisabledException.class})
    public ResponseEntity<ErrorResponse> credenciales(Exception ex) {
        return respuesta(HttpStatus.UNAUTHORIZED, MENSAJE_CREDENCIALES, null);
    }

    @ExceptionHandler(SesionInvalidaException.class)
    public ResponseEntity<ErrorResponse> sesionInvalida(SesionInvalidaException ex) {
        return respuesta(HttpStatus.UNAUTHORIZED, ex.getMessage(), null);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> bloqueada(LockedException ex) {
        return respuesta(HttpStatus.LOCKED, MENSAJE_BLOQUEO, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> inesperado(Exception ex) {
        log.error("Error no controlado en la API", ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado. Intente de nuevo o contacte al soporte.", null);
    }

    public static ErrorResponse cuerpo(HttpStatus status, String mensaje, Map<String, String> campos) {
        return new ErrorResponse(OffsetDateTime.now(), status.value(), status.getReasonPhrase(), mensaje, campos);
    }

    private ResponseEntity<ErrorResponse> respuesta(HttpStatus status, String mensaje, Map<String, String> campos) {
        return ResponseEntity.status(status).body(cuerpo(status, mensaje, campos));
    }
}

package com.pulsepass.exception;

import com.pulsepass.dto.response.ErrorResponse;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Unico punto donde las excepciones se convierten en respuestas HTTP
 * (PRD Fase 3, secciones 14 y 15, CTRL-007 / CTRL-008).
 *
 * <p>Los controllers no tienen bloques try/catch: lanzan o dejan pasar la
 * excepcion y este componente la traduce a {@link ErrorResponse}.
 *
 * <pre>
 * Excepcion                          HTTP  Uso
 * MethodArgumentNotValidException    400   Bean Validation
 * HttpMessageNotReadableException    400   JSON mal formado
 * ResourceNotFoundException          404   recurso inexistente
 * DuplicateResourceException         409   conflicto de unicidad
 * BusinessRuleException              409   regla de negocio incumplida
 * cualquier otra                     500   sin exponer detalles internos
 * </pre>
 *
 * <p>Se manejan ademas, de forma explicita, los errores propios de Spring MVC
 * (parametro faltante o de tipo incorrecto, metodo o media type no soportado,
 * ruta inexistente). Sin esto, el handler generico de {@link Exception} los
 * convertiria en un 500 incorrecto.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ------------------------------------------------------------------
    // 400 Bad Request
    // ------------------------------------------------------------------

    /** Bean Validation fallo sobre un @RequestBody: details contiene un par campo/mensaje por campo invalido. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        // TreeMap: salida ordenada y estable por nombre de campo.
        Map<String, String> details = new TreeMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            String message = fieldError.getDefaultMessage() != null
                    ? fieldError.getDefaultMessage()
                    : "Invalid value";
            details.putIfAbsent(fieldError.getField(), message);
        }
        return build(HttpStatus.BAD_REQUEST, "Validation failed", details);
    }

    /** Body ausente, JSON invalido, fecha ilegible o valor de enum desconocido. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
        // No se devuelve ex.getMessage(): trae detalles internos de Jackson.
        return build(HttpStatus.BAD_REQUEST, "Malformed JSON request", Map.of());
    }

    /** Falta un @RequestParam obligatorio (por ejemplo ?email=). */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex) {
        String parameter = ex.getParameterName();
        return build(HttpStatus.BAD_REQUEST,
                "Missing required parameter: " + parameter,
                detail(parameter, "Parameter is required"));
    }

    /** Un @PathVariable o @RequestParam no se puede convertir (por ejemplo /artists/abc). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String parameter = ex.getName();
        return build(HttpStatus.BAD_REQUEST,
                "Invalid value for parameter: " + parameter,
                detail(parameter, "Invalid value"));
    }

    // ------------------------------------------------------------------
    // 404 Not Found
    // ------------------------------------------------------------------

    /** El Service no encontro el recurso solicitado. */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), Map.of());
    }

    /** La ruta no existe en la API. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleUnknownEndpoint(NoResourceFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Endpoint not found", Map.of());
    }

    // ------------------------------------------------------------------
    // 405 / 415
    // ------------------------------------------------------------------

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method not supported for this endpoint", Map.of());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content type not supported, use application/json", Map.of());
    }

    // ------------------------------------------------------------------
    // 409 Conflict
    // ------------------------------------------------------------------

    /** Conflicto de unicidad detectado por el Service (username, email, evento, artista...). */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), Map.of());
    }

    /** Regla de negocio incumplida en el Service (edad minima, capacidad, transicion de estado...). */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), Map.of());
    }

    // ------------------------------------------------------------------
    // 401 / 403 Security
    // ------------------------------------------------------------------

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationFailure(AuthenticationException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Invalid identifier or password", Map.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "You are not allowed to perform this operation", Map.of());
    }

    // ------------------------------------------------------------------
    // 500 Internal Server Error
    // ------------------------------------------------------------------

    /**
     * Red de seguridad: cualquier excepcion no prevista. El detalle solo va al
     * log del servidor; el cliente recibe un mensaje generico (NFR-CTRL-007).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error while processing the request", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected internal error", Map.of());
    }

    /** Un unico par campo/mensaje; Map.of no admite claves nulas, por eso se protege el campo. */
    private static Map<String, String> detail(String field, String message) {
        return Map.of(field != null ? field : "parameter", message);
    }

    private static ResponseEntity<ErrorResponse> build(HttpStatus status,
                                                       String message,
                                                       Map<String, String> details) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                status.value(),
                status.getReasonPhrase(),
                message,
                details);
        return ResponseEntity.status(status).body(body);
    }
}

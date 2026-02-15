package com.lin.monkey.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/* Means it will catch errors from any and all the controllers
 * and turn them into JSON-response
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /* Spring exception for @Valid errors
     * Means that if there's a MethodArgumentNotValidException anywhere
     * in the app, this method will be called (like when dto validation failed)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException exception
    ) {
        /* Declaring a variable of Map interface (dictionary)
         * using concrete realization - HashMap
         */
        Map<String, String> errors = new HashMap<>();

        // Catching field errors
        exception.getBindingResult() // has validation result (errors) BindingResult obj
                .getAllErrors() // returns a List<ObjectError>
                .forEach(error -> {
                    // remaking a general ObjectError into a FieldError
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    errors.put(fieldName, errorMessage);
                });

        // errors Map will turn into JSON
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /* Handling service errors (IllegalArgumentException)
     * that were manually thrown
     */
    @ExceptionHandler(IllegalArgumentException.class)
    /* <Map<String, String>> means response body will be
     *  a JSON obj
     */
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        StackTraceElement[] stackTrace = exception.getStackTrace();
        String sourceLocation = "unknown";

        if (stackTrace.length > 0) {
            StackTraceElement top = stackTrace[0];
            sourceLocation = String.format("%s:%d",
                    top.getClassName() + "." + top.getMethodName(),
                    top.getLineNumber());
        }
        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Forbidden",
                "message", exception.getMessage() != null ? exception.getMessage() : "Illegal argument",
                "path", request.getRequestURI(),
                "source", sourceLocation
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        StackTraceElement[] stackTrace = exception.getStackTrace();
        String sourceLocation = "unknown";

        if (stackTrace.length > 0) {
            StackTraceElement top = stackTrace[0];
            sourceLocation = String.format("%s:%d",
                    top.getClassName() + "." + top.getMethodName(),
                    top.getLineNumber());
        }
        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.FORBIDDEN.value(),
                "error", "Forbidden",
                "message", exception.getMessage() != null ? exception.getMessage() : "Access denied",
                "path", request.getRequestURI(),
                "source", sourceLocation
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

}

package com.lin.monkey.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(
            IllegalArgumentException exception
    ) {
        Map<String, String> error = new HashMap<>();
        error.put("error", exception.getMessage());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

}

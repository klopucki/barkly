package pl.barkly.training.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

@RestControllerAdvice
public class ApiExceptionHandler {

    private final static Logger log = getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(BarklyException.class)
    public ResponseEntity<ErrorResponse> handle(
            BarklyException exception,
            HttpServletRequest request
    ) {

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                exception.getStatus().value(),
                exception.getStatus().getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI()
        );

        log.warn(
                "Business exception [{}]: {}",
                exception.getErrorCode(),
                exception.getMessage()
        );

        return ResponseEntity
                .status(exception.getStatus())
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
            MethodArgumentNotValidException exception
    ) {

        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest()
                .body(errors);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxUploadSize() {
        return ResponseEntity.badRequest()
                .body(Map.of("image", "Image exceeds the configured size limit"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception exception,
            HttpServletRequest request
    ) {

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                500,
                "Internal Server Error",
                "Unexpected error occurred",
                request.getRequestURI()
        );

        log.error("Unexpected error", exception);
        return ResponseEntity.internalServerError()
                .body(response);
    }

}

package pl.barkly.training.exceptions;

import org.springframework.http.HttpStatus;

public abstract class BarklyException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorCode errorCode;

    protected BarklyException(
            HttpStatus status,
            ErrorCode errorCode,
            String message
    ) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
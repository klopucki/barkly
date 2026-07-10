package pl.barkly.training.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidImageException extends BarklyException {

    public InvalidImageException(String message) {
        super(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, message);
    }
}

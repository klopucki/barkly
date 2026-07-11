package pl.barkly.training.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidTrainingConfigurationException extends BarklyException {
    public InvalidTrainingConfigurationException(String message) {
        super(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, message);
    }
}

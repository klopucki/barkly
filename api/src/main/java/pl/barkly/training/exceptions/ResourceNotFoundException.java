package pl.barkly.training.exceptions;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static pl.barkly.training.exceptions.ErrorCode.TRAINING_NOT_FOUND;

public class ResourceNotFoundException extends BarklyException {

    public ResourceNotFoundException(String message) {
        super(NOT_FOUND, TRAINING_NOT_FOUND, message);
    }

}
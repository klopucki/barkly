package pl.barkly.training.exceptions;

import org.springframework.http.HttpStatus;

public class TrainingRegistrationClosedException extends BarklyException {
    public TrainingRegistrationClosedException(Long trainingId) {
        super(
                HttpStatus.CONFLICT,
                ErrorCode.TRAINING_REGISTRATION_CLOSED,
                "Registration is closed because this training has already started: " + trainingId
        );
    }
}

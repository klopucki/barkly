package pl.barkly.training.exceptions;

import org.springframework.http.HttpStatus;

public class TrainingCapacityExceededException extends BarklyException {

    public TrainingCapacityExceededException(Long trainingId) {
        super(
                HttpStatus.CONFLICT,
                ErrorCode.TRAINING_CAPACITY_EXCEEDED,
                "Training %d is already full.".formatted(trainingId)
        );
    }

}
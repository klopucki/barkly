package pl.barkly.training.exceptions;

public class NoFreePlacesException extends RuntimeException {

    public NoFreePlacesException(Long trainingId, int capacity) {
        super("Training with id %d has reached its capacity (%d places)."
                .formatted(trainingId, capacity));
    }
}

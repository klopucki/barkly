package pl.barkly.training.persistence;

import pl.barkly.training.api.TrainingCreateRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

class TrainingDetailsFixtures {
    static final LocalDateTime START = LocalDateTime.of(2099, 5, 1, 12, 0);

    static TrainingCreateRequest details(int capacity, String description, int hoursLater) {
        return new TrainingCreateRequest(
                1L,
                "Trening",
                "Anna",
                START.plusHours(hoursLater),
                capacity,
                description,
                "TRAINING",
                "TO_BE_AGREED",
                null);
    }

    static TrainingEntity training() {
        return new TrainingEntity(details(5, null, 0));
    }

    static TrainingCreateRequest paidDetails() {
        return new TrainingCreateRequest(
                1L,
                "Trening",
                "Anna",
                START,
                5,
                "Opis",
                "TRAINING",
                "PAID",
                new BigDecimal("80.50"));
    }

    static TrainingEntity paidTraining() {
        return new TrainingEntity(paidDetails());
    }
}

package pl.barkly.training;

import java.time.LocalDate;

record TrainingResponse(
        Long id,
        String title,
        String description,
        String level,
        LocalDate date,
        int capacity
) {
    static TrainingResponse from(Training training) {
        return new TrainingResponse(
                training.id(),
                training.title(),
                training.description(),
                training.level().name(),
                training.date(),
                training.capacity()
        );
    }
}
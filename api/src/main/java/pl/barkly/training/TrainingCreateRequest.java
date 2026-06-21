package pl.barkly.training;

import java.time.LocalDate;

record TrainingCreateRequest(
        String title,
        String description,
        TrainingLevel level,
        LocalDate startAt,
        int capacity,
        String trainerName
) {
}

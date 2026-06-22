package pl.barkly.training;

import java.time.LocalDate;

record TrainingResponse(
        Long id,
        String title,
        String trainerName,
        String description,
        TrainingLevel level,
        LocalDate startAt,
        int capacity,
        int bookingCount
) {
}
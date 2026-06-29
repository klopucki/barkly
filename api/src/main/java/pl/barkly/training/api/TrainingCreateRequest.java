package pl.barkly.training.api;

import pl.barkly.training.TrainingLevel;

import java.time.LocalDateTime;

public record TrainingCreateRequest(
        Long schoolId,
        String title,
        String trainerName,
        TrainingLevel level,
        LocalDateTime startAt,
        Integer capacity,
        String description
) {
}

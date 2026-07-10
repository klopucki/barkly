package pl.barkly.training.api;

import pl.barkly.training.TrainingLevel;

import java.time.LocalDateTime;

public record TrainingResponse(
        Long id,
        Long schoolId,
        String title,
        String trainerName,
        TrainingLevel level,
        LocalDateTime startAt,
        Integer capacity,
        int bookedCount,
        String imageKey
) {
}

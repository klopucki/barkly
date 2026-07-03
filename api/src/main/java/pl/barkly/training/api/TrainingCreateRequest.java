package pl.barkly.training.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pl.barkly.training.TrainingLevel;

import java.time.LocalDateTime;

public record TrainingCreateRequest(
        Long schoolId,

        @NotBlank String title,
        String trainerName,
        @NotNull TrainingLevel level,
        @NotNull LocalDateTime startAt,
        Integer capacity,
        String description
) {
}

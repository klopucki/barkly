package pl.barkly.training.api;

import java.time.LocalDateTime;

public record MyTrainingBookingResponse(
        Long bookingId,
        Long trainingId,
        Long dogId,
        String dogName,
        String trainingTitle,
        String trainerName,
        String trainingType,
        LocalDateTime startAt
) {
}

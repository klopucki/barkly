package pl.barkly.training.api;

import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        Long trainingId,
        String ownerName,
        String email,
        String dogName,
        int dogAge,
        String notes,
        LocalDateTime createdAt
) {
}
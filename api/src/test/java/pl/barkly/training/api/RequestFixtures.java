package pl.barkly.training.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;

class RequestFixtures {
    static BookingCreateRequest invalidBooking() {
        return new BookingCreateRequest("A", "invalid", "", "x".repeat(1001));
    }

    static BookingCreateRequest validBooking() {
        return new BookingCreateRequest("Jan Kowalski", "jan@example.com", "Burek", null);
    }

    static TrainingCreateRequest invalidTraining() {
        return new TrainingCreateRequest(
                0L,
                "ab",
                "",
                LocalDateTime.now().minusDays(1),
                0,
                null,
                "TRAINING",
                "TO_BE_AGREED",
                null);
    }

    static TrainingCreateRequest training(String kind, String mode, String amount) {
        return new TrainingCreateRequest(
                1L,
                "Trening",
                "Anna",
                LocalDateTime.now().plusDays(1),
                null,
                null,
                kind,
                mode,
                amount == null ? null : new BigDecimal(amount));
    }

    static TrainingCreateRequest descriptionOfLength(int length) {
        return new TrainingCreateRequest(
                1L,
                "Trening",
                "Anna",
                LocalDateTime.now().plusDays(1),
                null,
                length < 0 ? null : "a".repeat(length),
                "TRAINING",
                "FREE",
                null);
    }

    static TrainingCreateRequest location(String address, Double latitude, Double longitude) {
        return new TrainingCreateRequest(
                1L,
                "Trening",
                "Anna",
                LocalDateTime.now().plusDays(1),
                null,
                null,
                "TRAINING",
                "FREE",
                null,
                false,
                address,
                latitude,
                longitude);
    }
}

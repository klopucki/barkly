package pl.barkly.training.api;

public record BookingCreateRequest(
        String ownerName,
        String email,
        String dogName,
        int dogAge,
        String notes
) {
}
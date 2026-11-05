package pl.barkly.training.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookingCreateRequest(
        @NotBlank(message = "Owner name is required")
                @Size(
                        min = 2,
                        max = 200,
                        message = "Owner name must contain between 2 and 200 characters")
                String ownerName,
        @NotBlank(message = "Email is required")
                @Email(message = "Email format is invalid")
                @Size(max = 320, message = "Email must not exceed 320 characters")
                String email,
        @NotBlank(message = "Dog name is required")
                @Size(
                        min = 2,
                        max = 200,
                        message = "Dog name must contain between 2 and 200 characters")
                String dogName,
        @Size(max = 1000, message = "Notes must not exceed 1000 characters") String notes) {}

package pl.barkly.training.api;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TrainingCreateRequest(
        @NotNull(message = "School is required") @Positive(message = "School id must be positive")
                Long schoolId,
        @NotBlank(message = "Title is required")
                @Size(
                        min = 3,
                        max = 200,
                        message = "Title must contain between 3 and 200 characters")
                String title,
        @NotBlank(message = "Trainer name is required")
                @Size(
                        min = 2,
                        max = 200,
                        message = "Trainer name must contain between 2 and 200 characters")
                String trainerName,
        @NotNull(message = "Start date is required")
                @Future(message = "Start date must be in the future")
                LocalDateTime startAt,
        @Positive(message = "Capacity must be greater than zero") Integer capacity,
        @Size(max = 2000, message = "Description must not exceed 2000 characters")
                String description,
        @Pattern(regexp = "TRAINING") String eventKind,
        @Pattern(regexp = "PAID|FREE|TO_BE_AGREED") String priceMode,
        @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal price) {
    @AssertTrue(message = "Paid training requires a price; other modes must have no price")
    public boolean isPriceValid() {
        return "PAID".equals(priceMode) ? price != null : price == null;
    }
}

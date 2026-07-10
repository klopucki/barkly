package pl.barkly.training.api;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pl.barkly.training.TrainingLevel;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CreateRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void rejectsInvalidBooking() {
        var request = new BookingCreateRequest("A", "invalid", "", 31, "x".repeat(1001));

        var invalidFields = validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(java.util.stream.Collectors.toSet());

        assertThat(invalidFields).contains("ownerName", "email", "dogName", "dogAge", "notes");
    }

    @Test
    void acceptsValidBooking() {
        var request = new BookingCreateRequest("Jan Kowalski", "jan@example.com", "Burek", 4, null);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejectsInvalidTraining() {
        var request = new TrainingCreateRequest(
                0L, "ab", "", null, LocalDateTime.now().minusDays(1), 0, null
        );

        var invalidFields = validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(java.util.stream.Collectors.toSet());

        assertThat(invalidFields).contains("schoolId", "title", "trainerName", "level", "startAt", "capacity");
    }

    @Test
    void acceptsValidTraining() {
        var request = new TrainingCreateRequest(
                1L,
                "Basic obedience",
                "Jan Kowalski",
                TrainingLevel.BASIC,
                LocalDateTime.now().plusDays(1),
                10,
                null
        );

        assertThat(validator.validate(request)).isEmpty();
    }
}

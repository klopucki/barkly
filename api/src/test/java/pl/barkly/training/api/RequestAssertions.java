package pl.barkly.training.api;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;

import java.util.Set;
import java.util.stream.Collectors;

class RequestAssertions {
    static void accepted(Set<? extends ConstraintViolation<?>> errors) {
        assertThat(errors).isEmpty();
    }

    static void rejectedFor(Set<? extends ConstraintViolation<?>> errors, String... fields) {
        var invalidFields =
                errors.stream()
                        .map(error -> error.getPropertyPath().toString())
                        .collect(Collectors.toSet());
        assertThat(invalidFields).containsExactlyInAnyOrder(fields);
    }
}

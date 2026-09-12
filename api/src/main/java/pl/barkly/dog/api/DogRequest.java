package pl.barkly.dog.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.barkly.dog.DogVisibility;

import java.time.LocalDate;

public record DogRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 150) String breed,
        LocalDate birthDate,
        @Size(max = 20) String sex,
        @Size(max = 4000) String description,
        @NotNull DogVisibility visibility
) {
}

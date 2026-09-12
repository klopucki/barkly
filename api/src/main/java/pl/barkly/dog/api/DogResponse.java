package pl.barkly.dog.api;

import pl.barkly.dog.DogVisibility;

import java.time.LocalDate;
import java.util.List;

public record DogResponse(
        Long id,
        String name,
        String breed,
        LocalDate birthDate,
        String sex,
        String description,
        DogVisibility visibility,
        boolean owner,
        List<DogImageResponse> images
) {
}

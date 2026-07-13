package pl.barkly.school.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SchoolCreateRequest(
        @NotBlank @Size(min = 2, max = 200) String name,
        @NotBlank @Size(max = 300) String address,
        @Size(max = 14) String krs,
        @Size(max = 5000) String description,
        @Size(max = 3000) String activities,
        @Size(max = 3000) String pricing
) {
}

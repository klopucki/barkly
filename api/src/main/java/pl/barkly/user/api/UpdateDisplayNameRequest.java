package pl.barkly.user.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Objects;

public record UpdateDisplayNameRequest(@NotBlank @Size(min = 2, max = 100) String displayName) {
    public UpdateDisplayNameRequest {
        if (Objects.nonNull(displayName)) displayName = displayName.trim();
    }
}

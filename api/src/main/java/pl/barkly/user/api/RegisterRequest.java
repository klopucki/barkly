package pl.barkly.user.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.barkly.user.UserRole;

public record RegisterRequest(
        @NotBlank @Email @Size(max = 320) String email,
        @NotBlank @Size(min = 10, max = 100) String password,
        @NotBlank @Size(min = 2, max = 100) String displayName,
        @NotNull UserRole role
) {
}

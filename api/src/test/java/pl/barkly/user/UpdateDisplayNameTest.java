package pl.barkly.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import jakarta.validation.Validation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import pl.barkly.user.api.UpdateDisplayNameRequest;

import java.util.Optional;

class UpdateDisplayNameTest {
    private final UserRepository users = mock(UserRepository.class);
    private final UserService service = new UserService(users, mock(PasswordEncoder.class));

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void updatesOnlyAuthenticatedActiveUser() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        UsernamePasswordAuthenticationToken.authenticated(
                                "user@example.com", "", java.util.List.of()));
        var user = new UserEntity("user@example.com", "hash", "Old", UserRole.USER);
        when(users.findLockedByEmail("user@example.com")).thenReturn(Optional.of(user));
        var result = service.updateDisplayName("New name");
        assertThat(result.displayName()).isEqualTo("New name");
        assertThat(user.getEmail()).isEqualTo("user@example.com");
        user.updateAccess(UserRole.USER, false);
        assertThatThrownBy(() -> service.updateDisplayName("Other"))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void validatesTrimmedName() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertThat(new UpdateDisplayNameRequest("  Ada  ").displayName()).isEqualTo("Ada");
            assertThat(validator.validate(new UpdateDisplayNameRequest("  a  "))).isNotEmpty();
            assertThat(validator.validate(new UpdateDisplayNameRequest("  "))).isNotEmpty();
            assertThat(validator.validate(new UpdateDisplayNameRequest("a".repeat(101))))
                    .isNotEmpty();
            assertThat(validator.validate(new UpdateDisplayNameRequest("Ada"))).isEmpty();
        }
    }
}

package pl.barkly.user.api;

import pl.barkly.user.UserEntity;
import pl.barkly.user.UserRole;

public record CurrentUserResponse(Long id, String email, String displayName, UserRole role) {
    public static CurrentUserResponse from(UserEntity user) {
        return new CurrentUserResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getRole());
    }
}

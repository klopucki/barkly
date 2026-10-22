package pl.barkly.user;

record AdminUserResponse(Long id, String email, String displayName, UserRole role, boolean active) {
    static AdminUserResponse from(UserEntity user) {
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole(),
                user.isActive());
    }
}

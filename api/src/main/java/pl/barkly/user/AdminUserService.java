package pl.barkly.user;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import pl.barkly.query.PageResponse;

@Service
class AdminUserService {
    private final UserRepository users;
    private final UserService currentUsers;

    AdminUserService(UserRepository users, UserService currentUsers) {
        this.users = users;
        this.currentUsers = currentUsers;
    }

    private UserEntity requireAdmin() {
        var actor = currentUsers.currentUser();
        if (!actor.isActive() || actor.getRole() != UserRole.SUPER_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return actor;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse> search(String query, int page, int size) {
        requireAdmin();
        String escaped = query.trim().replace("!", "!!").replace("%", "!%").replace("_", "!_");
        return PageResponse.from(
                users.search(escaped, PageRequest.of(page, size, Sort.by("id"))),
                AdminUserResponse::from);
    }

    @Transactional
    public AdminUserResponse update(long id, UserRole role, boolean active) {
        // Serialize administrative access changes before checking the actor's current permissions.
        users.lockAdministrators();
        var actor = requireAdmin();
        if (actor.getId().equals(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "You cannot change your own access");
        }
        var target =
                users.findLockedById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        target.updateAccess(role, active);
        return AdminUserResponse.from(target);
    }
}

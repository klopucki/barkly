package pl.barkly.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import pl.barkly.query.PageResponse;

@RestController
@RequestMapping("/api/admin/users")
class AdminUserController {
    private final AdminUserService users;

    AdminUserController(AdminUserService users) {
        this.users = users;
    }

    @GetMapping
    PageResponse<AdminUserResponse> search(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (page < 0 || size < 1 || size > 100 || query.length() > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return users.search(query, page, size);
    }

    @PutMapping("/{id}")
    AdminUserResponse update(
            @PathVariable long id, @Valid @RequestBody UpdateAccessRequest request) {
        return users.update(id, request.role(), request.active());
    }

    record UpdateAccessRequest(@NotNull UserRole role, @NotNull Boolean active) {}
}

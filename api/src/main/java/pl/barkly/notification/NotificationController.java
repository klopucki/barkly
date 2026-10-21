package pl.barkly.notification;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import pl.barkly.user.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/my/notifications")
class NotificationController {
    private final NotificationService notifications;
    private final UserService users;

    NotificationController(NotificationService notifications, UserService users) {
        this.notifications = notifications;
        this.users = users;
    }

    @GetMapping
    List<NotificationService.Item> list(@RequestParam(defaultValue = "0") int page) {
        return notifications.list(users.currentUser().getId(), page);
    }

    @GetMapping("/unread-count")
    long unreadCount() {
        return notifications.unreadCount(users.currentUser().getId());
    }

    @PutMapping("/{id}/read")
    void read(@PathVariable Long id) {
        if (!notifications.markRead(users.currentUser().getId(), id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/read-all")
    void readAll() {
        notifications.markAllRead(users.currentUser().getId());
    }

    @GetMapping("/preferences")
    Preferences preferences() {
        return new Preferences(notifications.remindersEnabled(users.currentUser().getId()));
    }

    @PutMapping("/preferences")
    Preferences preferences(@Valid @RequestBody Preferences request) {
        notifications.setRemindersEnabled(users.currentUser().getId(), request.remindersEnabled());
        return request;
    }

    record Preferences(@NotNull Boolean remindersEnabled) {}
}

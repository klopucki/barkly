package pl.barkly.notification;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import pl.barkly.user.UserEntity;
import pl.barkly.user.UserService;

class NotificationControllerTest {
    @Test
    void cannotMarkAnotherUsersNotificationAsRead() {
        var notifications = mock(NotificationService.class);
        var users = mock(UserService.class);
        var user = mock(UserEntity.class);
        when(users.currentUser()).thenReturn(user);
        when(user.getId()).thenReturn(12L);
        var controller = new NotificationController(notifications, users);
        assertThatThrownBy(() -> controller.read(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
        verify(notifications).markRead(12L, 99L);
    }
}

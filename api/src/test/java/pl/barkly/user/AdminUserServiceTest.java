package pl.barkly.user;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

class AdminUserServiceTest {
    private final UserRepository users = mock(UserRepository.class);
    private final UserService currentUsers = mock(UserService.class);
    private final AdminUserService service = new AdminUserService(users, currentUsers);

    private UserEntity actor(UserRole role) {
        var actor = mock(UserEntity.class);
        when(actor.getId()).thenReturn(1L);
        when(actor.getRole()).thenReturn(role);
        when(actor.isActive()).thenReturn(true);
        when(currentUsers.currentUser()).thenReturn(actor);
        return actor;
    }

    @Test
    void ordinaryUsersAndSchoolAdminsCannotReadOrChangeAccounts() {
        for (var role : new UserRole[] {UserRole.USER, UserRole.SCHOOL_ADMIN}) {
            actor(role);
            assertThatThrownBy(() -> service.search("", 0, 20))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("403");
            assertThatThrownBy(() -> service.update(2, UserRole.SUPER_ADMIN, true))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("403");
        }
        verify(users, never()).findLockedById(anyLong());
    }

    @Test
    void cannotChangeOwnRoleOrDeactivateOwnAccount() {
        actor(UserRole.SUPER_ADMIN);
        assertThatThrownBy(() -> service.update(1, UserRole.USER, true))
                .hasMessageContaining("409");
        assertThatThrownBy(() -> service.update(1, UserRole.SUPER_ADMIN, false))
                .hasMessageContaining("409");
        verify(users, never()).findLockedById(anyLong());
    }

    @Test
    void locksBeforeRecheckingAdminAndUpdatingTarget() {
        actor(UserRole.SUPER_ADMIN);
        var target = new UserEntity("user@example.com", "hash", "User", UserRole.USER);
        when(users.findLockedById(2L)).thenReturn(Optional.of(target));
        var result = service.update(2, UserRole.SCHOOL_ADMIN, false);
        assertThat(result.role()).isEqualTo(UserRole.SCHOOL_ADMIN);
        assertThat(result.active()).isFalse();
        assertThat(target.getSecurityVersion()).isEqualTo(1);
        var ordered = inOrder(users, currentUsers);
        ordered.verify(users).lockAdministrators();
        ordered.verify(currentUsers).currentUser();
        ordered.verify(users).findLockedById(2L);
        service.update(2, UserRole.SCHOOL_ADMIN, true);
        assertThat(target.getSecurityVersion()).isEqualTo(2);
        service.update(2, UserRole.SCHOOL_ADMIN, true);
        assertThat(target.getSecurityVersion()).isEqualTo(2);
    }

    @Test
    void missingAccountIsNotFound() {
        actor(UserRole.SUPER_ADMIN);
        assertThatThrownBy(() -> service.update(99, UserRole.USER, false))
                .hasMessageContaining("404");
    }

    @Test
    void searchEscapesWildcardCharactersAndReturnsPagination() {
        actor(UserRole.SUPER_ADMIN);
        when(users.search(eq("a!_!%!!"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(java.util.List.of()));
        assertThat(service.search(" a_%! ", 0, 20).items()).isEmpty();
        verify(users)
                .search(
                        eq("a!_!%!!"),
                        argThat(page -> page.getPageSize() == 20 && page.getPageNumber() == 0));
    }

    @Test
    void invalidPaginationIsRejected() {
        var controller = new AdminUserController(service);
        assertThatThrownBy(() -> controller.search("", -1, 20)).hasMessageContaining("400");
        assertThatThrownBy(() -> controller.search("", 0, 101)).hasMessageContaining("400");
        assertThatThrownBy(() -> controller.search("", 0, 0)).hasMessageContaining("400");
    }
}

package pl.barkly.user.access;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import pl.barkly.dog.DogEntity;
import pl.barkly.dog.DogRepository;
import pl.barkly.school.SchoolEntity;
import pl.barkly.school.SchoolRepository;
import pl.barkly.user.UserEntity;
import pl.barkly.user.UserRepository;
import pl.barkly.user.UserRole;
import pl.barkly.user.UserService;

import java.util.Optional;

class SharedAccessServiceTest {
    private final SchoolAccessInvitationRepository schools =
            mock(SchoolAccessInvitationRepository.class);
    private final DogAccessInvitationRepository dogs = mock(DogAccessInvitationRepository.class);
    private final SchoolRepository schoolRepository = mock(SchoolRepository.class);
    private final DogRepository dogRepository = mock(DogRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserService users = mock(UserService.class);
    private final SharedAccessService service =
            new SharedAccessService(
                    schools, dogs, schoolRepository, dogRepository, userRepository, users);

    @Test
    void onlyAcceptedDogInvitationGrantsAccess() {
        var owner = mock(UserEntity.class);
        var caregiver = mock(UserEntity.class);
        var dog = mock(DogEntity.class);
        when(owner.getId()).thenReturn(1L);
        when(caregiver.getId()).thenReturn(2L);
        when(dog.getId()).thenReturn(3L);
        when(dog.getOwner()).thenReturn(owner);
        assertThat(service.canManageDog(dog, caregiver)).isFalse();
        when(dogs.existsByDogIdAndInviteeUserIdAndStatus(3L, 2L, AccessStatus.ACCEPTED))
                .thenReturn(true);
        assertThat(service.canManageDog(dog, caregiver)).isTrue();
        assertThat(service.canManageDog(dog, owner)).isTrue();
    }

    @Test
    void anotherUserCannotAcceptSchoolInvitation() {
        var invitation = new SchoolAccessInvitation(1L, 2L, 3L);
        var other = mock(UserEntity.class);
        when(other.getId()).thenReturn(4L);
        when(users.currentUser()).thenReturn(other);
        when(schools.findById(8L)).thenReturn(Optional.of(invitation));
        assertThatThrownBy(() -> service.acceptSchool(8L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
        assertThat(invitation.getStatus()).isEqualTo(AccessStatus.PENDING);
    }

    @Test
    void onlyInviteeCanAcceptDogInvitation() {
        var invitation = new DogAccessInvitation(1L, 2L, 3L);
        var recipient = mock(UserEntity.class);
        when(recipient.getId()).thenReturn(2L);
        when(users.currentUser()).thenReturn(recipient);
        when(dogs.findById(8L)).thenReturn(Optional.of(invitation));
        service.acceptDog(8L);
        assertThat(invitation.getStatus()).isEqualTo(AccessStatus.ACCEPTED);
    }

    @Test
    void unrelatedUserCannotInviteAnotherDogCaregiver() {
        var dog = mock(DogEntity.class);
        var owner = mock(UserEntity.class);
        var stranger = mock(UserEntity.class);
        when(owner.getId()).thenReturn(1L);
        when(stranger.getId()).thenReturn(2L);
        when(users.currentUser()).thenReturn(stranger);
        when(dog.getOwner()).thenReturn(owner);
        when(dogRepository.findById(4L)).thenReturn(Optional.of(dog));
        assertThatThrownBy(() -> service.inviteDog(4L, "person@example.com"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
        verifyNoInteractions(userRepository);
    }

    @Test
    void unrelatedUserCannotRevokeDogCaregiver() {
        var invitation = new DogAccessInvitation(4L, 2L, 1L);
        var dog = mock(DogEntity.class);
        var owner = mock(UserEntity.class);
        var stranger = mock(UserEntity.class);
        when(owner.getId()).thenReturn(1L);
        when(stranger.getId()).thenReturn(3L);
        when(users.currentUser()).thenReturn(stranger);
        when(dog.getOwner()).thenReturn(owner);
        when(dogRepository.findById(4L)).thenReturn(Optional.of(dog));
        when(dogs.findById(8L)).thenReturn(Optional.of(invitation));
        assertThatThrownBy(() -> service.revokeDog(8L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
        verify(dogs, never()).delete(any());
    }

    @Test
    void primaryOwnerCanRevokeDogCaregiver() {
        var invitation = new DogAccessInvitation(4L, 2L, 1L);
        var dog = mock(DogEntity.class);
        var owner = mock(UserEntity.class);
        when(owner.getId()).thenReturn(1L);
        when(users.currentUser()).thenReturn(owner);
        when(dog.getOwner()).thenReturn(owner);
        when(dogRepository.findById(4L)).thenReturn(Optional.of(dog));
        when(dogs.findById(8L)).thenReturn(Optional.of(invitation));
        service.revokeDog(8L);
        verify(dogs).delete(invitation);
    }

    @Test
    void schoolAccessDoesNotRequireGlobalSchoolAdminRole() {
        var owner = mock(UserEntity.class);
        var user = mock(UserEntity.class);
        var school = mock(SchoolEntity.class);
        when(owner.getId()).thenReturn(1L);
        when(user.getId()).thenReturn(2L);
        when(user.getRole()).thenReturn(UserRole.USER);
        when(school.getOwner()).thenReturn(owner);
        when(school.getId()).thenReturn(5L);
        when(schools.existsBySchoolIdAndInviteeUserIdAndStatus(5L, 2L, AccessStatus.ACCEPTED))
                .thenReturn(true);
        assertThat(service.canManageSchool(school, user)).isTrue();
    }
}

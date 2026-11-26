package pl.barkly.school;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import pl.barkly.favorite.FavoriteService;
import pl.barkly.user.UserEntity;
import pl.barkly.user.UserService;
import pl.barkly.user.access.SharedAccessService;

import java.util.List;
import java.util.Optional;

class SchoolProfileImageTest {
    private final SchoolRepository schools = mock(SchoolRepository.class);
    private final SchoolImageRepository images = mock(SchoolImageRepository.class);
    private final UserService users = mock(UserService.class);
    private final SharedAccessService access = mock(SharedAccessService.class);
    private final SchoolService service =
            new SchoolService(schools, users, images, mock(FavoriteService.class), access);
    private final SchoolEntity school = mock(SchoolEntity.class);
    private final UserEntity user = mock(UserEntity.class);

    private void managedSchool() {
        when(schools.findWithOwnerById(1L)).thenReturn(Optional.of(school));
        when(school.getId()).thenReturn(1L);
        when(users.currentUser()).thenReturn(user);
        when(access.canManageSchool(school, user)).thenReturn(true);
    }

    @Test
    void managerCanSetImageBelongingToSchool() {
        managedSchool();
        var image = mock(SchoolImageEntity.class);
        when(image.getSchoolId()).thenReturn(1L);
        when(image.getId()).thenReturn(9L);
        when(image.getImageKey()).thenReturn("profile.jpg");
        when(images.findById(9L)).thenReturn(Optional.of(image));
        when(images.findAllBySchool_IdOrderByIdAsc(1L)).thenReturn(List.of(image));
        when(school.getProfileImageId()).thenReturn(9L);
        assertThat(service.setProfileImage(1L, 9L).profileImageKey()).isEqualTo("profile.jpg");
        verify(school).setProfileImageId(9L);
    }

    @Test
    void rejectsAnotherSchoolsImage() {
        managedSchool();
        var image = mock(SchoolImageEntity.class);
        when(image.getSchoolId()).thenReturn(2L);
        when(images.findById(9L)).thenReturn(Optional.of(image));
        assertThatThrownBy(() -> service.setProfileImage(1L, 9L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
        verify(school, never()).setProfileImageId(any());
    }

    @Test
    void rejectsUserWithoutManagementAccess() {
        managedSchool();
        when(access.canManageSchool(school, user)).thenReturn(false);
        assertThatThrownBy(() -> service.setProfileImage(1L, 9L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
        verifyNoInteractions(images);
    }
}

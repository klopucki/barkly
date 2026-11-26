package pl.barkly.school;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import pl.barkly.favorite.FavoriteService;
import pl.barkly.school.api.SchoolCreateRequest;
import pl.barkly.user.UserEntity;
import pl.barkly.user.UserService;
import pl.barkly.user.access.SharedAccessService;

import java.util.List;
import java.util.Optional;

class SchoolImageSeparationTest {
    private final SchoolRepository schools = mock(SchoolRepository.class);
    private final SchoolImageRepository images = mock(SchoolImageRepository.class);
    private final UserService users = mock(UserService.class);
    private final SharedAccessService access = mock(SharedAccessService.class);
    private final UserEntity user = mock(UserEntity.class);
    private final SchoolEntity school =
            spy(
                    new SchoolEntity(
                            new SchoolCreateRequest("School", "Address", null, "", "", "", ""),
                            user));
    private final SchoolService service =
            new SchoolService(schools, users, images, mock(FavoriteService.class), access);

    private void allowManagement() {
        doReturn(1L).when(school).getId();
        when(schools.findWithOwnerById(1L)).thenReturn(Optional.of(school));
        when(users.currentUser()).thenReturn(user);
        when(access.canManageSchool(school, user)).thenReturn(true);
    }

    private SchoolImageEntity image(long id, String key, boolean gallery) {
        var image = spy(new SchoolImageEntity(school, key, gallery));
        doReturn(id).when(image).getId();
        return image;
    }

    @Test
    void replacingProfileDoesNotAddOldOrNewPhotoToGallery() {
        allowManagement();
        var oldProfile = image(8L, "old.jpg", true);
        var newProfile = image(9L, "new.jpg", false);
        var gallery = image(10L, "gallery.jpg", true);
        school.setProfileImageId(8L);
        when(images.findById(8L)).thenReturn(Optional.of(oldProfile));
        when(images.findById(9L)).thenReturn(Optional.of(newProfile));
        when(images.findAllBySchool_IdOrderByIdAsc(1L))
                .thenReturn(List.of(oldProfile, newProfile, gallery));

        var response = service.setProfileImage(1L, 9L);

        assertThat(response.profileImageKey()).isEqualTo("new.jpg");
        assertThat(response.images()).extracting("imageKey").containsExactly("gallery.jpg");
        assertThat(oldProfile.isGalleryVisible()).isFalse();
        assertThat(newProfile.isGalleryVisible()).isFalse();
        assertThat(oldProfile.getImageKey()).isEqualTo("old.jpg");
    }

    @Test
    void hidingLegacyGalleryImageDoesNotDeleteTheImage() {
        allowManagement();
        var image = image(9L, "article.jpg", true);
        when(images.findById(9L)).thenReturn(Optional.of(image));
        when(images.findAllBySchool_IdOrderByIdAsc(1L)).thenReturn(List.of(image));
        var response = service.hideImageFromGallery(9L);
        assertThat(response.images()).isEmpty();
        assertThat(response.profileImageKey()).isEqualTo("article.jpg");
        assertThat(school.getProfileImageId()).isEqualTo(9L);
        assertThat(image.isGalleryVisible()).isFalse();
        verify(images, never()).delete(any());
    }

    @Test
    void changingHistoryPreservesTheExistingImplicitProfile() {
        allowManagement();
        var oldHistory = image(8L, "old.jpg", true);
        var newHistory = image(9L, "new.jpg", false);
        school.setHistoryImageId(8L);
        when(images.findById(8L)).thenReturn(Optional.of(oldHistory));
        when(images.findById(9L)).thenReturn(Optional.of(newHistory));
        when(images.findAllBySchool_IdOrderByIdAsc(1L)).thenReturn(List.of(oldHistory, newHistory));
        var response = service.setHistoryImage(1L, 9L);
        assertThat(response.profileImageKey()).isEqualTo("old.jpg");
        assertThat(response.historyImageKey()).isEqualTo("new.jpg");
        assertThat(response.images()).isEmpty();
    }

    @Test
    void anotherUserCannotHideAnImage() {
        allowManagement();
        var image = image(9L, "photo.jpg", true);
        when(images.findById(9L)).thenReturn(Optional.of(image));
        when(access.canManageSchool(school, user)).thenReturn(false);
        assertThatThrownBy(() -> service.hideImageFromGallery(9L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
        assertThat(image.isGalleryVisible()).isTrue();
    }
}

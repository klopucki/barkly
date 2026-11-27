package pl.barkly.dog;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import pl.barkly.training.image.TrainingImageStorage;
import pl.barkly.user.UserEntity;
import pl.barkly.user.UserService;
import pl.barkly.user.access.SharedAccessService;

import java.util.List;
import java.util.Optional;

class DogPostPrivacyTest {
    private final DogRepository dogs = mock(DogRepository.class);
    private final DogPostRepository posts = mock(DogPostRepository.class);
    private final UserService users = mock(UserService.class);
    private final TrainingImageStorage storage = mock(TrainingImageStorage.class);
    private final DogPostReactionRepository reactions = mock(DogPostReactionRepository.class);
    private final DogPostCommentRepository comments = mock(DogPostCommentRepository.class);
    private final SharedAccessService access = mock(SharedAccessService.class);
    private final DogPostService service =
            new DogPostService(dogs, posts, users, storage, reactions, comments, access);
    private final UserEntity owner = mock(UserEntity.class);
    private final UserEntity other = mock(UserEntity.class);
    private final DogEntity dog = mock(DogEntity.class);
    private final DogPostEntity post = mock(DogPostEntity.class);

    private void setup(DogVisibility visibility, UserEntity viewer) {
        when(owner.getId()).thenReturn(1L);
        when(other.getId()).thenReturn(2L);
        when(users.currentUser()).thenReturn(viewer);
        when(users.optionalCurrentUser()).thenReturn(Optional.of(viewer));
        when(dog.getOwner()).thenReturn(owner);
        when(dog.getVisibility()).thenReturn(visibility);
        when(post.getDog()).thenReturn(dog);
        when(post.getAuthor()).thenReturn(owner);
        when(post.getId()).thenReturn(7L);
        when(post.getImageKey()).thenReturn("private.jpg");
        when(posts.findWithDogById(7L)).thenReturn(Optional.of(post));
        when(posts.findFeed()).thenReturn(List.of(post));
        when(access.canManageDog(dog, owner)).thenReturn(true);
    }

    @Test
    void privatePostCommentsAndImageAreHiddenFromAnotherUser() {
        setup(DogVisibility.PRIVATE, other);
        assertThat(service.feed()).isEmpty();
        assertThatThrownBy(() -> service.comments(7L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
        assertThatThrownBy(() -> service.loadImage(7L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
        verify(storage, never()).load(anyString());
    }

    @Test
    void ownerAndSignedInMembersCanSeeAllowedPostsAndImages() {
        setup(DogVisibility.PRIVATE, owner);
        when(storage.load("private.jpg")).thenReturn(new ByteArrayResource(new byte[] {1}));
        assertThat(service.feed()).hasSize(1);
        assertThat(service.loadImage(7L)).isNotNull();
        when(users.currentUser()).thenReturn(other);
        when(users.optionalCurrentUser()).thenReturn(Optional.of(other));
        when(dog.getVisibility()).thenReturn(DogVisibility.MEMBERS);
        assertThat(service.feed()).hasSize(1);
        assertThat(service.comments(7L)).isEmpty();
        assertThat(service.loadImage(7L)).isNotNull();
    }

    @Test
    void onlyAuthorCanDeleteAndImageIsRemovedAfterCommit() {
        setup(DogVisibility.PUBLIC, other);
        assertThatThrownBy(() -> service.delete(7L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
        verify(posts, never()).delete(any());
        when(users.currentUser()).thenReturn(owner);
        TransactionSynchronizationManager.initSynchronization();
        try {
            service.delete(7L);
            verify(posts).delete(post);
            verify(storage, never()).delete(anyString());
            TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCommit());
            verify(storage).delete("private.jpg");
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}

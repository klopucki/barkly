package pl.barkly.dog;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import pl.barkly.dog.api.DogPostResponse;
import pl.barkly.training.image.TrainingImageStorage;
import pl.barkly.user.UserEntity;
import pl.barkly.user.UserService;

import java.util.List;

@Service
class DogPostService {
    private final DogRepository dogs;
    private final DogPostRepository posts;
    private final UserService users;
    private final TrainingImageStorage storage;

    DogPostService(DogRepository dogs, DogPostRepository posts, UserService users, TrainingImageStorage storage) {
        this.dogs = dogs; this.posts = posts; this.users = users; this.storage = storage;
    }

    @Transactional(readOnly = true)
    List<DogPostResponse> feed() {
        UserEntity user = users.optionalCurrentUser().orElse(null);
        return posts.findFeed().stream().filter(post -> canView(post.getDog(), user)).map(this::response).toList();
    }

    @Transactional
    DogPostResponse create(Long dogId, String content, MultipartFile image) {
        DogEntity dog = dogs.findById(dogId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dog not found"));
        UserEntity user = users.currentUser();
        if (!dog.getOwner().getId().equals(user.getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot post for this dog");
        String text = content == null ? "" : content.trim();
        if (text.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post content is required");
        String imageKey = image == null || image.isEmpty() ? null : storage.store(image);
        try {
            return response(posts.save(new DogPostEntity(dog, text, imageKey)));
        } catch (RuntimeException exception) {
            if (imageKey != null) storage.delete(imageKey);
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    Resource loadImage(Long id) { return storage.load(visiblePost(id).getImageKey()); }

    @Transactional(readOnly = true)
    MediaType imageContentType(Long id) {
        String key = visiblePost(id).getImageKey();
        if (key.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (key.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        return MediaType.IMAGE_JPEG;
    }

    private DogPostEntity visiblePost(Long id) {
        DogPostEntity post = posts.findWithDogById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        if (post.getImageKey() == null || !canView(post.getDog(), users.optionalCurrentUser().orElse(null))) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        return post;
    }

    private boolean canView(DogEntity dog, UserEntity user) {
        return (user != null && dog.getOwner().getId().equals(user.getId())) || dog.getVisibility() == DogVisibility.PUBLIC || (dog.getVisibility() == DogVisibility.MEMBERS && user != null);
    }

    private DogPostResponse response(DogPostEntity post) {
        return new DogPostResponse(post.getId(), post.getDog().getId(), post.getDog().getName(), post.getDog().getOwner().getDisplayName(), post.getContent(), post.getImageKey() != null, post.getPublishedAt());
    }
}

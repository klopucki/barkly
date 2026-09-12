package pl.barkly.dog;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import pl.barkly.dog.api.DogImageResponse;
import pl.barkly.dog.api.DogRequest;
import pl.barkly.dog.api.DogResponse;
import pl.barkly.training.image.TrainingImageStorage;
import pl.barkly.user.UserEntity;
import pl.barkly.user.UserService;

import java.util.List;
import java.util.Optional;

@Service
class DogService {

    private final DogRepository dogs;
    private final DogImageRepository images;
    private final UserService users;
    private final TrainingImageStorage storage;

    DogService(DogRepository dogs, DogImageRepository images, UserService users,
               TrainingImageStorage storage) {
        this.dogs = dogs;
        this.images = images;
        this.users = users;
        this.storage = storage;
    }

    @Transactional(readOnly = true)
    List<DogResponse> mine() {
        UserEntity user = users.currentUser();
        return dogs.findAllByOwner_IdOrderByNameAsc(user.getId()).stream()
                .map(dog -> response(dog, user))
                .toList();
    }

    @Transactional(readOnly = true)
    List<DogResponse> visible() {
        Optional<UserEntity> user = users.optionalCurrentUser();
        return dogs.findAllByOrderByNameAsc().stream()
                .filter(dog -> canView(dog.getVisibility(), dog, user.orElse(null)))
                .map(dog -> response(dog, user.orElse(null)))
                .toList();
    }

    @Transactional(readOnly = true)
    DogResponse one(Long id) {
        DogEntity dog = find(id);
        UserEntity user = users.optionalCurrentUser().orElse(null);
        requireVisible(dog.getVisibility(), dog, user);
        return response(dog, user);
    }

    @Transactional
    DogResponse create(DogRequest request) {
        UserEntity owner = users.currentUser();
        DogEntity dog = dogs.save(new DogEntity(
                owner, request.name().trim(), trim(request.breed()), request.birthDate(),
                trim(request.sex()), text(request.description()), request.visibility()
        ));
        return response(dog, owner);
    }

    @Transactional
    DogResponse update(Long id, DogRequest request) {
        DogEntity dog = find(id);
        UserEntity user = users.currentUser();
        requireOwner(dog, user);
        dog.update(
                request.name().trim(), trim(request.breed()), request.birthDate(),
                trim(request.sex()), text(request.description()), request.visibility()
        );
        return response(dog, user);
    }

    @Transactional
    void delete(Long id) {
        DogEntity dog = find(id);
        requireOwner(dog, users.currentUser());
        images.findAllByDog_IdOrderByIdAsc(id).forEach(image -> storage.delete(image.getImageKey()));
        dogs.delete(dog);
    }

    @Transactional
    DogImageResponse addImage(Long id, MultipartFile file, DogVisibility visibility) {
        DogEntity dog = find(id);
        requireOwner(dog, users.currentUser());
        String key = storage.store(file);
        try {
            DogImageEntity image = images.save(new DogImageEntity(dog, key, visibility));
            return new DogImageResponse(image.getId(), image.getVisibility());
        } catch (RuntimeException exception) {
            storage.delete(key);
            throw exception;
        }
    }

    @Transactional
    void deleteImage(Long id) {
        DogImageEntity image = images.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dog image not found"));
        requireOwner(image.getDog(), users.currentUser());
        images.delete(image);
        storage.delete(image.getImageKey());
    }

    @Transactional(readOnly = true)
    org.springframework.core.io.Resource loadImage(Long id) {
        DogImageEntity image = findImage(id);
        requireVisible(image.getVisibility(), image.getDog(), users.optionalCurrentUser().orElse(null));
        return storage.load(image.getImageKey());
    }

    @Transactional(readOnly = true)
    MediaType imageContentType(Long id) {
        DogImageEntity image = findImage(id);
        requireVisible(image.getVisibility(), image.getDog(), users.optionalCurrentUser().orElse(null));
        if (image.getImageKey().endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (image.getImageKey().endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }
        return MediaType.IMAGE_JPEG;
    }

    private DogEntity find(Long id) {
        return dogs.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dog not found"));
    }

    private DogImageEntity findImage(Long id) {
        return images.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dog image not found"));
    }

    private boolean canView(DogVisibility visibility, DogEntity dog, UserEntity user) {
        return (user != null && dog.getOwner().getId().equals(user.getId()))
                || visibility == DogVisibility.PUBLIC
                || (visibility == DogVisibility.MEMBERS && user != null);
    }

    private void requireVisible(DogVisibility visibility, DogEntity dog, UserEntity user) {
        if (!canView(visibility, dog, user)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dog not found");
        }
    }

    private void requireOwner(DogEntity dog, UserEntity user) {
        if (!dog.getOwner().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot manage this dog");
        }
    }

    private DogResponse response(DogEntity dog, UserEntity user) {
        boolean owner = user != null && dog.getOwner().getId().equals(user.getId());
        List<DogImageResponse> visibleImages = images.findAllByDog_IdOrderByIdAsc(dog.getId()).stream()
                .filter(image -> canView(image.getVisibility(), dog, user))
                .map(image -> new DogImageResponse(image.getId(), image.getVisibility()))
                .toList();
        return new DogResponse(
                dog.getId(), dog.getName(), dog.getBreed(), dog.getBirthDate(), dog.getSex(),
                dog.getDescription(), dog.getVisibility(), owner, visibleImages
        );
    }

    private String trim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String text(String value) {
        return value == null ? "" : value.trim();
    }
}

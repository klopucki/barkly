package pl.barkly.dog;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pl.barkly.dog.api.DogImageResponse;
import pl.barkly.dog.api.DogRequest;
import pl.barkly.dog.api.DogResponse;

import java.util.List;

@RestController
class DogController {
    private final DogService dogs;

    DogController(DogService dogs) {
        this.dogs = dogs;
    }

    @GetMapping("/api/dogs")
    List<DogResponse> visible() {
        return dogs.visible();
    }

    @GetMapping("/api/dogs/{id}")
    DogResponse one(@PathVariable Long id) {
        return dogs.one(id);
    }

    @GetMapping("/api/my/dogs")
    List<DogResponse> mine() {
        return dogs.mine();
    }

    @PostMapping("/api/dogs")
    DogResponse create(@Valid @RequestBody DogRequest request) {
        return dogs.create(request);
    }

    @PutMapping("/api/dogs/{id}")
    DogResponse update(@PathVariable Long id, @Valid @RequestBody DogRequest request) {
        return dogs.update(id, request);
    }

    @DeleteMapping("/api/dogs/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id) {
        dogs.delete(id);
    }

    @PostMapping(path = "/api/dogs/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    DogImageResponse image(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile image,
            @RequestParam("visibility") DogVisibility visibility
    ) {
        return dogs.addImage(id, image, visibility);
    }

    @DeleteMapping("/api/dog-images/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteImage(@PathVariable Long id) {
        dogs.deleteImage(id);
    }
}

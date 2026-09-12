package pl.barkly.dog;

import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
class DogImageController {
    private final DogService dogs;

    DogImageController(DogService dogs) {
        this.dogs = dogs;
    }

    @GetMapping("/api/dog-images/{id}")
    ResponseEntity<Resource> load(@PathVariable Long id) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(dogs.imageContentType(id))
                .body(dogs.loadImage(id));
    }
}

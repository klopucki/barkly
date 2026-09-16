package pl.barkly.dog;

import jakarta.validation.constraints.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pl.barkly.dog.api.DogPostResponse;

import java.util.List;

@RestController
class DogPostController {
    private final DogPostService posts;

    DogPostController(DogPostService posts) { this.posts = posts; }

    @GetMapping("/api/dog-posts")
    List<DogPostResponse> feed() { return posts.feed(); }

    @PostMapping(path = "/api/dog-posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    DogPostResponse create(@RequestParam @NotNull Long dogId, @RequestParam String content,
                           @RequestParam(required = false) MultipartFile image) {
        return posts.create(dogId, content, image);
    }

    @GetMapping("/api/dog-post-images/{id}")
    ResponseEntity<Resource> image(@org.springframework.web.bind.annotation.PathVariable Long id) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).contentType(posts.imageContentType(id)).body(posts.loadImage(id));
    }
}

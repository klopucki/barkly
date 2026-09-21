package pl.barkly.dog;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pl.barkly.dog.api.DogPostResponse;
import pl.barkly.dog.api.DogPostCommentRequest;
import pl.barkly.dog.api.DogPostCommentResponse;
import pl.barkly.dog.api.PostReactionRequest;

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

    @PutMapping("/api/dog-posts/{id}/reaction")
    DogPostResponse react(@PathVariable Long id, @Valid @RequestBody PostReactionRequest request) { return posts.react(id, request); }

    @GetMapping("/api/dog-posts/{id}/comments")
    List<DogPostCommentResponse> comments(@PathVariable Long id) { return posts.comments(id); }

    @PostMapping("/api/dog-posts/{id}/comments")
    DogPostCommentResponse comment(@PathVariable Long id, @Valid @RequestBody DogPostCommentRequest request) { return posts.addComment(id, request); }

    @DeleteMapping("/api/dog-post-comments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteComment(@PathVariable Long id) { posts.deleteComment(id); }
}

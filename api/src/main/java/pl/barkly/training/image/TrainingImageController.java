package pl.barkly.training.image;

import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pl.barkly.training.api.TrainingResponse;

import java.util.concurrent.TimeUnit;

@RestController
class TrainingImageController {

    private final TrainingImageService imageService;

    TrainingImageController(TrainingImageService imageService) {
        this.imageService = imageService;
    }

    @PutMapping(path = "/api/trainings/{trainingId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    TrainingResponse replace(
            @PathVariable Long trainingId,
            @RequestParam("image") MultipartFile image
    ) {
        return imageService.replace(trainingId, image);
    }

    @GetMapping("/api/training-images/{key}")
    ResponseEntity<Resource> load(@PathVariable String key) {
        return ResponseEntity.ok()
                .contentType(contentType(key))
                .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable())
                .body(imageService.load(key));
    }

    private MediaType contentType(String key) {
        if (key.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (key.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }
        return MediaType.IMAGE_JPEG;
    }
}

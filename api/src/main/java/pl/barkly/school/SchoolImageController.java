package pl.barkly.school;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pl.barkly.school.api.SchoolImageResponse;
import pl.barkly.training.image.TrainingImageStorage;

@RestController
class SchoolImageController {
    private final SchoolService schools;
    private final TrainingImageStorage storage;
    SchoolImageController(SchoolService schools, TrainingImageStorage storage) { this.schools = schools; this.storage = storage; }

    @PostMapping(path = "/api/schools/{schoolId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    SchoolImageResponse add(@PathVariable Long schoolId, @RequestParam("image") MultipartFile image) {
        return schools.addImage(schoolId, image, storage);
    }

    @DeleteMapping("/api/school-images/{imageId}")
    void delete(@PathVariable Long imageId) { schools.deleteImage(imageId, storage); }
}

package pl.barkly.school;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.barkly.school.api.SchoolCreateRequest;
import pl.barkly.school.api.SchoolResponse;

import java.util.List;

@RestController
class SchoolController {
    private final SchoolService schoolService;

    SchoolController(SchoolService schoolService) { this.schoolService = schoolService; }

    @GetMapping("/api/schools")
    List<SchoolResponse> findAll() { return schoolService.findAll(); }

    @GetMapping("/api/my/schools")
    List<SchoolResponse> findMine() { return schoolService.findMine(); }

    @GetMapping("/api/schools/{slug}")
    SchoolResponse findBySlug(@PathVariable String slug) { return schoolService.findBySlug(slug); }

    @PostMapping("/api/schools")
    SchoolResponse create(@Valid @RequestBody SchoolCreateRequest request) { return schoolService.create(request); }

    @PutMapping("/api/schools/{id}")
    SchoolResponse update(@PathVariable Long id, @Valid @RequestBody SchoolCreateRequest request) {
        return schoolService.update(id, request);
    }
}

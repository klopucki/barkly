package pl.barkly.training;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
class TrainingController {

    private final TrainingService trainingService;

    TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @GetMapping("/api/trainings")
    List<TrainingResponse> findTrainings() {
        return trainingService.findAll();
    }

    @GetMapping("/api/trainings/{id}")
    TrainingResponse findTraining(@PathVariable Long id) {
        return trainingService.findById(id);
    }

    @PostMapping("/api/trainings")
    TrainingResponse createTraining(@RequestBody TrainingCreateRequest request) {
        return trainingService.create(request);
    }
}
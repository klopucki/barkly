package pl.barkly.training;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
class TrainingController {

    private final TrainingService trainingService;

    TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @GetMapping("/api/trainings")
    List<Training> findTrainings() {
        return trainingService.findAll();
    }
}
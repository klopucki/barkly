package pl.barkly.training;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.barkly.training.api.TrainingCreateRequest;
import pl.barkly.training.api.TrainingResponse;

import java.util.List;

@RestController
class TrainingController {

    private final TrainingFacade trainingFacade;

    TrainingController(TrainingFacade trainingFacade) {
        this.trainingFacade = trainingFacade;
    }

    @GetMapping("/api/trainings")
    List<TrainingResponse> findAll() {
        return trainingFacade.findTrainings();
    }

    @GetMapping("/api/trainings/{id}")
    TrainingResponse findById(@PathVariable Long id) {
        return trainingFacade.findTraining(id);
    }

    @PostMapping("/api/trainings")
    TrainingResponse create(@Valid @RequestBody TrainingCreateRequest request) {
        return trainingFacade.createTraining(request);
    }

    @DeleteMapping("/api/trainings/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTraining(@PathVariable Long id) {
        trainingFacade.deleteTraining(id);
    }
}

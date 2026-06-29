package pl.barkly.training.application;

import org.springframework.stereotype.Service;
import pl.barkly.training.api.TrainingCreateRequest;
import pl.barkly.training.api.TrainingResponse;
import pl.barkly.training.persistence.TrainingEntity;
import pl.barkly.training.persistence.TrainingRepository;

@Service
class TrainingCommandService {

    private final TrainingRepository trainingRepository;

    TrainingCommandService(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    TrainingResponse create(TrainingCreateRequest request) {
        TrainingEntity saved = trainingRepository.save(new TrainingEntity(request));
        return saved.toResponse(0);
    }
}
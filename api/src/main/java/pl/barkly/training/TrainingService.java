package pl.barkly.training;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
class TrainingService {

    private final TrainingRepository trainingRepository;

    TrainingService(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    List<TrainingResponse> findAll() {
        return trainingRepository.findAll()
                .stream()
                .map(TrainingEntity::toResponse)
                .toList();
    }

    TrainingResponse findById(Long id) {
        return trainingRepository.findById(id)
                .map(TrainingEntity::toResponse)
                .orElseThrow();
    }

    TrainingResponse create(TrainingCreateRequest request) {
        TrainingEntity training = TrainingEntity.create(request);

        TrainingEntity saved = trainingRepository.save(training);

        return saved.toResponse();
    }
}
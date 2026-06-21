package pl.barkly.training;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
                .map(TrainingEntity::toDomain)
                .map(TrainingResponse::from)
                .toList();
    }

    TrainingResponse findById(Long id) {
        return trainingRepository.findById(id)
                .map(TrainingEntity::toDomain)
                .map(TrainingResponse::from)
                .orElseThrow();
    }

    TrainingResponse create(TrainingCreateRequest request) {
        TrainingEntity saved = trainingRepository.save(TrainingEntity.from(request));
        return TrainingResponse.from(saved.toDomain());
    }
}
package pl.barkly.training.application;

import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.barkly.training.api.TrainingCreateRequest;
import pl.barkly.training.api.TrainingResponse;
import pl.barkly.training.persistence.BookingRepository;
import pl.barkly.training.persistence.TrainingEntity;
import pl.barkly.training.persistence.TrainingRepository;

import java.time.LocalDateTime;

@Service
class TrainingCommandService {

    private final TrainingRepository trainingRepository;
    private final BookingRepository bookingRepository;

    TrainingCommandService(
            TrainingRepository trainingRepository,
            BookingRepository bookingRepository
    ) {
        this.trainingRepository = trainingRepository;
        this.bookingRepository = bookingRepository;
    }

    TrainingResponse create(TrainingCreateRequest request) {
        TrainingEntity saved = trainingRepository.save(new TrainingEntity(request));
        return saved.toResponse(0);
    }

    @Transactional
    public void deleteTraining(Long id) {
        TrainingEntity training = trainingRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        LocalDateTime deletedAt = LocalDateTime.now();

        training.softDelete();
        bookingRepository.softDeleteByTrainingId(id, deletedAt);
    }
}
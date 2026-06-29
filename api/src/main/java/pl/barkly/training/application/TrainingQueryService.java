package pl.barkly.training.application;

import org.springframework.stereotype.Service;
import pl.barkly.training.api.TrainingResponse;
import pl.barkly.training.persistence.BookingRepository;
import pl.barkly.training.persistence.TrainingEntity;
import pl.barkly.training.persistence.TrainingRepository;

import java.util.List;

@Service
class TrainingQueryService {

    private final TrainingRepository trainingRepository;
    private final BookingRepository bookingRepository;

    TrainingQueryService(
            TrainingRepository trainingRepository,
            BookingRepository bookingRepository
    ) {
        this.trainingRepository = trainingRepository;
        this.bookingRepository = bookingRepository;
    }

    List<TrainingResponse> findAll() {
        return trainingRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    TrainingResponse findById(Long id) {
        return trainingRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow();
    }

    private TrainingResponse toResponse(TrainingEntity training) {
        int bookedCount = bookingRepository.countByTrainingId(training.getId());
        return training.toResponse(bookedCount);
    }
}
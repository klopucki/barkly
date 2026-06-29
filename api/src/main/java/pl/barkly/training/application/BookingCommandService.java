package pl.barkly.training.application;

import org.springframework.stereotype.Service;
import pl.barkly.training.api.BookingCreateRequest;
import pl.barkly.training.api.BookingResponse;
import pl.barkly.training.persistence.BookingEntity;
import pl.barkly.training.persistence.BookingRepository;
import pl.barkly.training.persistence.TrainingEntity;
import pl.barkly.training.persistence.TrainingRepository;

@Service
class BookingCommandService {

    private final TrainingRepository trainingRepository;
    private final BookingRepository bookingRepository;

    BookingCommandService(
            TrainingRepository trainingRepository,
            BookingRepository bookingRepository
    ) {
        this.trainingRepository = trainingRepository;
        this.bookingRepository = bookingRepository;
    }

    BookingResponse book(Long trainingId, BookingCreateRequest request) {
        TrainingEntity training = trainingRepository.findById(trainingId)
                .orElseThrow();

        int bookedCount = bookingRepository.countByTrainingId(trainingId);

        training.validateBooking(bookedCount);

        BookingEntity saved =
                bookingRepository.save(new BookingEntity(trainingId, request));

        return saved.toResponse();
    }
}
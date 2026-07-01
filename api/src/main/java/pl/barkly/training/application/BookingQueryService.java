package pl.barkly.training.application;

import org.springframework.stereotype.Service;
import pl.barkly.training.api.BookingResponse;
import pl.barkly.training.persistence.BookingEntity;
import pl.barkly.training.persistence.BookingRepository;

import java.util.List;

@Service
class BookingQueryService {

    private final BookingRepository bookingRepository;

    BookingQueryService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    List<BookingResponse> findByTrainingId(Long trainingId) {
        return bookingRepository.findAllByTrainingIdAndDeletedAtIsNull(trainingId)
                .stream()
                .map(BookingEntity::toResponse)
                .toList();
    }
}
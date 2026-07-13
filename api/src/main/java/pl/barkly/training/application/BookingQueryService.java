package pl.barkly.training.application;

import org.springframework.stereotype.Service;
import pl.barkly.training.api.BookingResponse;
import pl.barkly.training.persistence.BookingEntity;
import pl.barkly.training.persistence.BookingRepository;
import pl.barkly.training.persistence.TrainingRepository;
import pl.barkly.school.SchoolService;

import java.util.List;

@Service
class BookingQueryService {

    private final BookingRepository bookingRepository;
    private final TrainingRepository trainingRepository;
    private final SchoolService schoolService;

    BookingQueryService(BookingRepository bookingRepository, TrainingRepository trainingRepository, SchoolService schoolService) {
        this.bookingRepository = bookingRepository;
        this.trainingRepository = trainingRepository;
        this.schoolService = schoolService;
    }

    List<BookingResponse> findByTrainingId(Long trainingId) {
        var training = trainingRepository.findByIdAndDeletedAtIsNull(trainingId).orElseThrow();
        schoolService.requireManagementAccess(schoolService.findEntityWithOwner(training.toResponse().schoolId()));
        return bookingRepository.findAllByTrainingIdAndDeletedAtIsNull(trainingId)
                .stream()
                .map(BookingEntity::toResponse)
                .toList();
    }
}

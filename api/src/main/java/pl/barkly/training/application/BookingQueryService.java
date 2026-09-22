package pl.barkly.training.application;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Service;
import pl.barkly.school.SchoolService;
import pl.barkly.dog.DogRepository;
import pl.barkly.training.api.BookingResponse;
import pl.barkly.training.api.MyTrainingBookingResponse;
import pl.barkly.training.persistence.BookingEntity;
import pl.barkly.training.persistence.BookingRepository;
import pl.barkly.training.persistence.TrainingEntity;
import pl.barkly.training.persistence.TrainingRepository;
import pl.barkly.user.UserService;

@Service
class BookingQueryService {

    private final BookingRepository bookingRepository;
    private final TrainingRepository trainingRepository;
    private final SchoolService schoolService;
    private final DogRepository dogRepository;
    private final UserService userService;

    BookingQueryService(
            BookingRepository bookingRepository,
            TrainingRepository trainingRepository,
            SchoolService schoolService,
            DogRepository dogRepository,
            UserService userService
    ) {
        this.bookingRepository = bookingRepository;
        this.trainingRepository = trainingRepository;
        this.schoolService = schoolService;
        this.dogRepository = dogRepository;
        this.userService = userService;
    }

    List<BookingResponse> findByTrainingId(Long trainingId) {
        var training = trainingRepository.findByIdAndDeletedAtIsNull(trainingId).orElseThrow();
        schoolService.requireManagementAccess(schoolService.findEntityWithOwner(training.toResponse().schoolId()));
        return bookingRepository.findAllByTrainingIdAndDeletedAtIsNull(trainingId)
                .stream()
                .map(BookingEntity::toResponse)
                .toList();
    }

    List<MyTrainingBookingResponse> findMine() {
        var dogs = dogRepository.findAllByOwner_IdOrderByNameAsc(userService.currentUser().getId());
        if (dogs.isEmpty()) {
            return List.of();
        }

        var bookings = bookingRepository.findAllByDogIdInAndDeletedAtIsNull(
                dogs.stream().map(dog -> dog.getId()).toList()
        );
        if (bookings.isEmpty()) {
            return List.of();
        }

        var trainings = trainingRepository.findAllByIdInAndDeletedAtIsNull(
                bookings.stream().map(BookingEntity::getTrainingId).distinct().toList()
        ).stream().collect(java.util.stream.Collectors.toMap(
                TrainingEntity::getId,
                Function.identity()
        ));

        return bookings.stream()
                .map(booking -> toCalendarEntry(booking, trainings))
                .filter(java.util.Objects::nonNull)
                .sorted(java.util.Comparator.comparing(MyTrainingBookingResponse::startAt))
                .toList();
    }

    private MyTrainingBookingResponse toCalendarEntry(
            BookingEntity booking,
            Map<Long, TrainingEntity> trainings
    ) {
        var training = trainings.get(booking.getTrainingId());
        if (training == null) {
            return null;
        }
        return new MyTrainingBookingResponse(
                booking.getId(),
                training.getId(),
                booking.getDogId(),
                booking.getDogName(),
                training.getTitle(),
                training.getTrainerName(),
                training.getTrainingTypeName(),
                training.getStartAt()
        );
    }
}

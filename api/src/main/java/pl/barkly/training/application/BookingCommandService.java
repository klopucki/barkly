package pl.barkly.training.application;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.barkly.training.api.BookingCreateRequest;
import pl.barkly.training.api.BookingResponse;
import pl.barkly.training.persistence.BookingEntity;
import pl.barkly.training.persistence.BookingRepository;
import pl.barkly.training.persistence.TrainingEntity;
import pl.barkly.training.persistence.TrainingRepository;
import pl.barkly.school.SchoolService;
import pl.barkly.dog.DogRepository;
import pl.barkly.user.UserService;
import java.time.Period;

@Service
class BookingCommandService {

    private final TrainingRepository trainingRepository;
    private final BookingRepository bookingRepository;
    private final SchoolService schoolService;
    private final DogRepository dogs;
    private final UserService users;

    BookingCommandService(
            TrainingRepository trainingRepository,
            BookingRepository bookingRepository,
            SchoolService schoolService, DogRepository dogs, UserService users
    ) {
        this.trainingRepository = trainingRepository;
        this.bookingRepository = bookingRepository;
        this.schoolService = schoolService;
        this.dogs = dogs; this.users = users;
    }

    BookingResponse book(Long trainingId, BookingCreateRequest request) {
        TrainingEntity training = trainingRepository.findById(trainingId)
                .orElseThrow();

        int bookedCount = bookingRepository.countByTrainingIdAndDeletedAtIsNull(trainingId);

        training.validateBooking(bookedCount);

        BookingEntity saved =
                bookingRepository.save(new BookingEntity(trainingId, request));

        return saved.toResponse();
    }

    @Transactional
    BookingResponse quickBook(Long trainingId, Long dogId) {
        TrainingEntity training = trainingRepository.findByIdAndDeletedAtIsNull(trainingId).orElseThrow();
        var user = users.currentUser();
        var dog = dogs.findById(dogId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dog not found"));
        if (!dog.getOwner().getId().equals(user.getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only register your own dog");
        if (bookingRepository.existsByTrainingIdAndDogIdAndDeletedAtIsNull(trainingId, dogId)) throw new ResponseStatusException(HttpStatus.CONFLICT, "This dog is already registered for this training");
        training.validateBooking(bookingRepository.countByTrainingIdAndDeletedAtIsNull(trainingId));
        int age = dog.getBirthDate() == null ? 0 : Math.max(0, Period.between(dog.getBirthDate(), java.time.LocalDate.now()).getYears());
        return bookingRepository.save(new BookingEntity(trainingId, dogId, user.getDisplayName(), user.getEmail(), dog.getName(), age)).toResponse();
    }

    @Transactional
    void deleteBooking(Long id) {
        BookingEntity booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        TrainingEntity training = trainingRepository.findByIdAndDeletedAtIsNull(booking.getTrainingId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        schoolService.requireManagementAccess(schoolService.findEntityWithOwner(training.toResponse().schoolId()));

        booking.softDelete();
    }
}

package pl.barkly.training.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import pl.barkly.notification.NotificationService;
import pl.barkly.school.SchoolEntity;
import pl.barkly.school.SchoolService;
import pl.barkly.training.api.TrainingCreateRequest;
import pl.barkly.training.image.TrainingImageStorage;
import pl.barkly.training.persistence.BookingRepository;
import pl.barkly.training.persistence.TrainingEntity;
import pl.barkly.training.persistence.TrainingRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

class TrainingCommandFixtures {
    final TrainingRepository trainings = mock(TrainingRepository.class);
    final BookingRepository bookings = mock(BookingRepository.class);
    final SchoolService schools = mock(SchoolService.class);
    final NotificationService notifications = mock(NotificationService.class);
    final SchoolEntity school = mock(SchoolEntity.class);
    final TrainingCommandService service =
            new TrainingCommandService(
                    trainings, bookings, mock(TrainingImageStorage.class), schools, notifications);
    final LocalDateTime start = LocalDateTime.of(2099, 1, 1, 10, 0);
    TrainingEntity entity;

    TrainingCommandFixtures() {
        when(schools.findEntityWithOwner(1L)).thenReturn(school);
    }

    TrainingCreateRequest request(String mode, String amount, LocalDateTime date) {
        return new TrainingCreateRequest(
                1L,
                "Trening grupy",
                "Anna",
                date,
                null,
                "Zabierz smaczki",
                "TRAINING",
                mode,
                amount == null ? null : new BigDecimal(amount));
    }

    static TrainingCommandFixtures managedSchool() {
        var scenario = new TrainingCommandFixtures();
        when(scenario.trainings.save(any(TrainingEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        return scenario;
    }

    static TrainingCommandFixtures schoolWithoutAccess() {
        var scenario = new TrainingCommandFixtures();
        denyAccess(scenario);
        return scenario;
    }

    static TrainingCommandFixtures existingPaidTraining() {
        var scenario = new TrainingCommandFixtures();
        scenario.entity = new TrainingEntity(scenario.request("PAID", "80.50", scenario.start));
        when(scenario.trainings.findByIdAndDeletedAtIsNull(7L))
                .thenReturn(Optional.of(scenario.entity));
        when(scenario.bookings.countByTrainingIdAndDeletedAtIsNull(7L)).thenReturn(3);
        return scenario;
    }

    static TrainingCommandFixtures trainingWithoutAccess() {
        var scenario = existingPaidTraining();
        denyAccess(scenario);
        return scenario;
    }

    static TrainingCommandFixtures missingTraining() {
        var scenario = new TrainingCommandFixtures();
        when(scenario.trainings.findByIdAndDeletedAtIsNull(7L)).thenReturn(Optional.empty());
        return scenario;
    }

    private static void denyAccess(TrainingCommandFixtures scenario) {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN))
                .when(scenario.schools)
                .requireManagementAccess(scenario.school);
    }
}

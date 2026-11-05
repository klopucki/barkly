package pl.barkly.notification;

import static org.mockito.Mockito.*;

import pl.barkly.school.SchoolEntity;
import pl.barkly.school.SchoolService;
import pl.barkly.training.api.TrainingResponse;
import pl.barkly.training.persistence.BookingRepository;
import pl.barkly.training.persistence.TrainingEntity;
import pl.barkly.training.persistence.TrainingRepository;
import pl.barkly.user.UserEntity;
import pl.barkly.user.UserRole;
import pl.barkly.user.UserService;

import java.util.Optional;

class TrainingMessageFixtures {
    final SchoolService schools = mock(SchoolService.class);
    final TrainingRepository trainings = mock(TrainingRepository.class);
    final NotificationService notifications = mock(NotificationService.class);
    final BookingRepository bookings = mock(BookingRepository.class);
    final UserService users = mock(UserService.class);
    final UserEntity user = mock(UserEntity.class);
    final SchoolMessageController controller =
            new SchoolMessageController(schools, trainings, notifications, bookings, users);

    static TrainingMessageFixtures forUser(UserRole role, boolean owner, boolean booked) {
        var f = new TrainingMessageFixtures();
        var training = mock(TrainingEntity.class);
        var response = mock(TrainingResponse.class);
        var school = mock(SchoolEntity.class);
        var schoolOwner = mock(UserEntity.class);
        when(f.users.currentUser()).thenReturn(f.user);
        when(f.user.getId()).thenReturn(9L);
        when(f.user.getRole()).thenReturn(role);
        when(f.trainings.findByIdAndDeletedAtIsNull(7L)).thenReturn(Optional.of(training));
        when(training.toResponse()).thenReturn(response);
        when(response.schoolId()).thenReturn(1L);
        when(f.schools.findEntityWithOwner(1L)).thenReturn(school);
        when(school.getOwner()).thenReturn(schoolOwner);
        when(schoolOwner.getId()).thenReturn(owner ? 9L : 10L);
        when(f.bookings.existsByTrainingIdAndUserIdAndDeletedAtIsNull(7L, 9L)).thenReturn(booked);
        return f;
    }
}

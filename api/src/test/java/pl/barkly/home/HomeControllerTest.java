package pl.barkly.home;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import pl.barkly.school.SchoolRepository;
import pl.barkly.training.persistence.BookingRepository;
import pl.barkly.training.persistence.TrainingRepository;
import pl.barkly.user.UserEntity;
import pl.barkly.user.UserService;
import pl.barkly.user.access.SharedAccessService;

import java.time.LocalDateTime;
import java.util.List;

class HomeControllerTest {
    @Test
    void summaryUsesCurrentUserAndLimitsEveryQuery() {
        var bookings = mock(BookingRepository.class);
        var trainings = mock(TrainingRepository.class);
        var users = mock(UserService.class);
        var schools = mock(SchoolRepository.class);
        var access = mock(SharedAccessService.class);
        var user = mock(UserEntity.class);
        when(users.currentUser()).thenReturn(user);
        when(user.getId()).thenReturn(42L);
        var training =
                new HomeSummary.SchoolTraining(
                        4L, "Training", LocalDateTime.now(), 8, 2L, 3L, "School");
        when(trainings.findUpcomingForManagedSchools(eq(42L), any(), eq(PageRequest.of(0, 4))))
                .thenReturn(List.of(training));

        var result = new HomeController(bookings, trainings, users, schools, access).summary();

        assertThat(result.schoolTrainings()).containsExactly(training);
        verify(bookings).findUpcomingForAccessibleDogs(eq(42L), any(), eq(PageRequest.of(0, 4)));
        verify(bookings).findRecentForManagedSchools(eq(42L), any(), eq(PageRequest.of(0, 4)));
        verify(trainings).findUpcomingForManagedSchools(eq(42L), any(), eq(PageRequest.of(0, 4)));
    }
}

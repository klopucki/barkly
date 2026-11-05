package pl.barkly.notification;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import pl.barkly.school.SchoolEntity;
import pl.barkly.school.SchoolService;
import pl.barkly.training.api.TrainingResponse;
import pl.barkly.training.persistence.TrainingEntity;
import pl.barkly.training.persistence.TrainingRepository;

import java.util.Optional;

class SchoolMessageControllerTest {
    private final SchoolService schools = mock(SchoolService.class);
    private final TrainingRepository trainings = mock(TrainingRepository.class);
    private final NotificationService notifications = mock(NotificationService.class);
    private final SchoolEntity school = mock(SchoolEntity.class);
    private final SchoolMessageController controller =
            new SchoolMessageController(
                    schools,
                    trainings,
                    notifications,
                    mock(pl.barkly.training.persistence.BookingRepository.class),
                    mock(pl.barkly.user.UserService.class));

    @Test
    void authorizedOwnerCanMessageSchool() {
        when(schools.findEntityWithOwner(1L)).thenReturn(school);
        when(school.getSlug()).thenReturn("szkola");
        controller.schoolMessage(1L, new SchoolMessageController.Message("  Cześć  "));
        verify(schools).requireManagementAccess(school);
        verify(notifications).messageSchool(1L, "Cześć", "/schools/szkola");
    }

    @Test
    void unauthorizedOwnerCannotMessageSchool() {
        when(schools.findEntityWithOwner(1L)).thenReturn(school);
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN))
                .when(schools)
                .requireManagementAccess(school);
        assertThatThrownBy(
                        () ->
                                controller.schoolMessage(
                                        1L, new SchoolMessageController.Message("Cześć")))
                .isInstanceOf(ResponseStatusException.class);
        verifyNoInteractions(notifications);
    }

    @Test
    void trainingMessageChecksTheTrainingSchool() {
        var training = mock(TrainingEntity.class);
        var response = mock(TrainingResponse.class);
        when(trainings.findByIdAndDeletedAtIsNull(7L)).thenReturn(Optional.of(training));
        when(training.toResponse()).thenReturn(response);
        when(response.schoolId()).thenReturn(1L);
        when(schools.findEntityWithOwner(1L)).thenReturn(school);
        controller.trainingMessage(7L, new SchoolMessageController.Message("Cześć"));
        verify(schools).requireManagementAccess(school);
        verify(notifications).messageTraining(7L, "Cześć");
    }
}

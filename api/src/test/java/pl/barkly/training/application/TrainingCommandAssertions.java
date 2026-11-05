package pl.barkly.training.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import pl.barkly.training.api.TrainingResponse;

class TrainingCommandAssertions {
    static void paidTrainingCreated(TrainingCommandFixtures s, TrainingResponse response) {
        assertThat(response.price()).isEqualByComparingTo("80.50");
        assertThat(response.priceMode()).isEqualTo("PAID");
        assertThat(response.eventKind()).isEqualTo("TRAINING");
        assertThat(response.description()).isEqualTo("Zabierz smaczki");
        assertThat(response.capacity()).isNull();
        assertThat(response.bookedCount()).isZero();
        verify(s.schools).requireManagementAccess(s.school);
    }

    static void creationForbidden(TrainingCommandFixtures s, Throwable failure) {
        status(failure, HttpStatus.FORBIDDEN);
        verifyNoInteractions(s.trainings, s.notifications);
    }

    static void priceChangedAndParticipantsNotified(
            TrainingCommandFixtures s, TrainingResponse response, String price) {
        assertThat(response.price()).isEqualByComparingTo(price);
        assertThat(response.bookedCount()).isEqualTo(3);
        verify(s.notifications).trainingChanged(s.entity, false, false);
    }

    static void freeTrainingAndParticipantsNotified(
            TrainingCommandFixtures s, TrainingResponse response) {
        assertThat(response.price()).isNull();
        assertThat(response.priceMode()).isEqualTo("FREE");
        verify(s.notifications).trainingChanged(s.entity, false, false);
    }

    static void noNotifications(TrainingCommandFixtures s) {
        verifyNoInteractions(s.notifications);
    }

    static void postponedAndParticipantsNotified(
            TrainingCommandFixtures s, TrainingResponse response) {
        assertThat(response.startAt()).isEqualTo(s.start.plusDays(1));
        verify(s.notifications).trainingChanged(s.entity, false, true);
    }

    static void editingForbiddenWithoutChanges(TrainingCommandFixtures s, Throwable failure) {
        status(failure, HttpStatus.FORBIDDEN);
        assertThat(s.entity.toResponse().price()).isEqualByComparingTo("80.50");
        assertThat(s.entity.toResponse().priceMode()).isEqualTo("PAID");
        verifyNoInteractions(s.notifications);
    }

    static void trainingNotFound(TrainingCommandFixtures s, Throwable failure) {
        status(failure, HttpStatus.NOT_FOUND);
        verifyNoInteractions(s.notifications, s.bookings);
    }

    private static void status(Throwable failure, HttpStatus status) {
        assertThat(failure)
                .isInstanceOfSatisfying(
                        ResponseStatusException.class,
                        error -> assertThat(error.getStatusCode()).isEqualTo(status));
    }
}

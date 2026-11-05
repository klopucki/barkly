package pl.barkly.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class TrainingMessageAssertions {
    static void allowed(TrainingMessageFixtures f) {
        verify(f.notifications).trainingMessages(7L, 0);
    }

    static void forbidden(TrainingMessageFixtures f, Throwable error) {
        assertThat(error)
                .isInstanceOfSatisfying(
                        ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
        verifyNoInteractions(f.notifications);
    }
}

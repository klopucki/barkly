package pl.barkly.training.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import pl.barkly.training.api.TrainingCreateRequest;

import java.time.LocalDateTime;

class TrainingNotificationDetailsTest {
    private final LocalDateTime start = LocalDateTime.of(2030, 5, 1, 12, 0);

    private TrainingCreateRequest request(LocalDateTime date, int capacity, String description) {
        return new TrainingCreateRequest(
                1L, "Trening", "Anna", 1L, null, null, false, date, capacity, description);
    }

    private TrainingEntity training() {
        var type = new TrainingTypeEntity();
        ReflectionTestUtils.setField(type, "id", 1L);
        return new TrainingEntity(request(start, 5, null), type, null, null);
    }

    @Test
    void unchangedDetailsAndCapacityChangesDoNotNotifyParticipants() {
        assertThat(training().notificationDetailsChanged(request(start, 5, ""))).isFalse();
        assertThat(training().notificationDetailsChanged(request(start, 8, null))).isFalse();
    }

    @Test
    void dateAndDescriptionChangesNotifyParticipants() {
        assertThat(training().notificationDetailsChanged(request(start.plusHours(1), 5, null)))
                .isTrue();
        assertThat(
                        training()
                                .notificationDetailsChanged(
                                        request(start, 5, "Nowe miejsce spotkania")))
                .isTrue();
    }
}

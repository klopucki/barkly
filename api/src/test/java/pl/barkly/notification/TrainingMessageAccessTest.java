package pl.barkly.notification;

import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.Test;

import pl.barkly.user.UserRole;

class TrainingMessageAccessTest {
    @Test
    void participantCanReadHistory() {
        var fixture = TrainingMessageFixtures.forUser(UserRole.USER, false, true);
        fixture.controller.trainingMessages(7L, 0);
        TrainingMessageAssertions.allowed(fixture);
    }

    @Test
    void ownerCanReadHistoryWithoutBooking() {
        var fixture = TrainingMessageFixtures.forUser(UserRole.SCHOOL_ADMIN, true, false);
        fixture.controller.trainingMessages(7L, 0);
        TrainingMessageAssertions.allowed(fixture);
    }

    @Test
    void unrelatedUserCannotReadHistory() {
        var fixture = TrainingMessageFixtures.forUser(UserRole.USER, false, false);
        var error = catchThrowable(() -> fixture.controller.trainingMessages(7L, 0));
        TrainingMessageAssertions.forbidden(fixture, error);
    }

    @Test
    void otherSchoolOwnerCannotReadHistory() {
        var fixture = TrainingMessageFixtures.forUser(UserRole.SCHOOL_ADMIN, false, false);
        var error = catchThrowable(() -> fixture.controller.trainingMessages(7L, 0));
        TrainingMessageAssertions.forbidden(fixture, error);
    }
}

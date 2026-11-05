package pl.barkly.training.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TrainingNotificationDetailsTest {
    @ParameterizedTest
    @CsvSource({"5,'',0,false", "8,,0,false", "5,,1,true", "5,Nowe miejsce spotkania,0,true"})
    void notifiesOnlyWhenRelevantDetailsChange(
            int capacity, String description, int hoursLater, boolean expected) {
        var training = TrainingDetailsFixtures.training();
        var request = TrainingDetailsFixtures.details(capacity, description, hoursLater);
        var notify = training.notificationDetailsChanged(request);
        TrainingDetailsAssertions.notificationDecision(notify, expected);
    }

    @Test
    void priceChangeRequiresNotification() {
        var training = TrainingDetailsFixtures.training();
        var request = TrainingDetailsFixtures.paidDetails();

        var notify = training.notificationDetailsChanged(request);

        TrainingDetailsAssertions.notificationDecision(notify, true);
    }

    @Test
    void priceAndDescriptionSurviveUpdate() {
        var training = TrainingDetailsFixtures.training();
        var request = TrainingDetailsFixtures.paidDetails();

        training.update(request);

        TrainingDetailsAssertions.paidDetailsPreserved(training.toResponse());
    }

    @Test
    void unchangedPaidDetailsDoNotRequireNotification() {
        var training = TrainingDetailsFixtures.paidTraining();
        var request = TrainingDetailsFixtures.paidDetails();

        var notify = training.notificationDetailsChanged(request);

        TrainingDetailsAssertions.notificationDecision(notify, false);
    }

    @Test
    void switchingToPriceToBeAgreedClearsAmount() {
        var training = TrainingDetailsFixtures.paidTraining();
        var request = TrainingDetailsFixtures.details(5, null, 0);

        training.update(request);

        TrainingDetailsAssertions.priceCleared(training.toResponse());
    }
}

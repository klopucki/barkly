package pl.barkly.training.application;

import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.Test;

class TrainingCommandServiceTest {
    @Test
    void createsPaidTrainingWithoutDictionariesAndReturnsItsDetails() {
        var fixture = TrainingCommandFixtures.managedSchool();
        var request = fixture.request("PAID", "80.50", fixture.start);

        var response = fixture.service.create(request);

        TrainingCommandAssertions.paidTrainingCreated(fixture, response);
    }

    @Test
    void deniesCreationBeforeSavingWhenSchoolAccessIsMissing() {
        var fixture = TrainingCommandFixtures.schoolWithoutAccess();
        var request = fixture.request("FREE", null, fixture.start);

        var failure = catchThrowable(() -> fixture.service.create(request));

        TrainingCommandAssertions.creationForbidden(fixture, failure);
    }

    @Test
    void priceOnlyChangeNotifiesParticipantsWithoutDateChange() {
        var fixture = TrainingCommandFixtures.existingPaidTraining();
        var request = fixture.request("PAID", "90.00", fixture.start);

        var response = fixture.service.update(7L, request);

        TrainingCommandAssertions.priceChangedAndParticipantsNotified(fixture, response, "90.00");
    }

    @Test
    void switchingToFreeClearsPriceAndNotifiesParticipants() {
        var fixture = TrainingCommandFixtures.existingPaidTraining();
        var request = fixture.request("FREE", null, fixture.start);

        var response = fixture.service.update(7L, request);

        TrainingCommandAssertions.freeTrainingAndParticipantsNotified(fixture, response);
    }

    @Test
    void equivalentPriceWithDifferentScaleDoesNotNotify() {
        var fixture = TrainingCommandFixtures.existingPaidTraining();
        var request = fixture.request("PAID", "80.5", fixture.start);

        fixture.service.update(7L, request);

        TrainingCommandAssertions.noNotifications(fixture);
    }

    @Test
    void changingDateTriggersDateChangeNotification() {
        var fixture = TrainingCommandFixtures.existingPaidTraining();
        var request = fixture.request("PAID", "80.50", fixture.start.plusDays(1));

        var response = fixture.service.update(7L, request);

        TrainingCommandAssertions.postponedAndParticipantsNotified(fixture, response);
    }

    @Test
    void deniesEditingWithoutMutatingTrainingOrNotifyingParticipants() {
        var fixture = TrainingCommandFixtures.trainingWithoutAccess();
        var request = fixture.request("FREE", null, fixture.start);

        var failure = catchThrowable(() -> fixture.service.update(7L, request));

        TrainingCommandAssertions.editingForbiddenWithoutChanges(fixture, failure);
    }

    @Test
    void missingOrDeletedTrainingCannotBeEdited() {
        var fixture = TrainingCommandFixtures.missingTraining();
        var request = fixture.request("FREE", null, fixture.start);

        var failure = catchThrowable(() -> fixture.service.update(7L, request));

        TrainingCommandAssertions.trainingNotFound(fixture, failure);
    }
}

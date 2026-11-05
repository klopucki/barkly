package pl.barkly.training.api;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class CreateRequestValidationTest {
    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    @Test
    void rejectsInvalidBooking() {
        var request = RequestFixtures.invalidBooking();
        var errors = validator.validate(request);
        RequestAssertions.rejectedFor(errors, "ownerName", "email", "dogName", "notes");
    }

    @Test
    void acceptsValidBooking() {
        var request = RequestFixtures.validBooking();
        var errors = validator.validate(request);
        RequestAssertions.accepted(errors);
    }

    @Test
    void rejectsInvalidTraining() {
        var request = RequestFixtures.invalidTraining();
        var errors = validator.validate(request);
        RequestAssertions.rejectedFor(
                errors, "schoolId", "title", "trainerName", "startAt", "capacity");
    }

    @ParameterizedTest
    @CsvSource({
        "EXAM,FREE,,eventKind", "WORKSHOP,FREE,,eventKind", "TRAINING,UNKNOWN,,priceMode",
        "TRAINING,PAID,-0.01,price", "TRAINING,PAID,0,price", "TRAINING,PAID,100000000,price",
        "TRAINING,PAID,1.001,price", "TRAINING,PAID,,priceValid", "TRAINING,FREE,1,priceValid",
        "TRAINING,TO_BE_AGREED,1,priceValid"
    })
    void rejectsInvalidEventOrPrice(String kind, String mode, String amount, String field) {
        var request = RequestFixtures.training(kind, mode, amount);
        var errors = validator.validate(request);
        RequestAssertions.rejectedFor(errors, field);
    }

    @ParameterizedTest
    @CsvSource({"PAID,0.01", "PAID,80.50", "PAID,99999999.99", "FREE,", "TO_BE_AGREED,"})
    void acceptsSupportedPricesWithoutDictionaries(String mode, String amount) {
        var request = RequestFixtures.training("TRAINING", mode, amount);
        var errors = validator.validate(request);
        RequestAssertions.accepted(errors);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 2000})
    void acceptsOptionalDescriptionWithinLimit(int length) {
        var request = RequestFixtures.descriptionOfLength(length);
        var errors = validator.validate(request);
        RequestAssertions.accepted(errors);
    }

    @Test
    void rejectsDescriptionOverLimit() {
        var request = RequestFixtures.descriptionOfLength(2001);
        var errors = validator.validate(request);
        RequestAssertions.rejectedFor(errors, "description");
    }

    @Test
    void acceptsOptionalLocationAndCoordinates() {
        RequestAssertions.accepted(validator.validate(RequestFixtures.location(null, null, null)));
        RequestAssertions.accepted(
                validator.validate(RequestFixtures.location("Park", null, null)));
        RequestAssertions.accepted(
                validator.validate(RequestFixtures.location(null, 52.23, 21.01)));
    }

    @Test
    void rejectsIncompleteOrOutOfRangeCoordinates() {
        RequestAssertions.rejectedFor(
                validator.validate(RequestFixtures.location(null, 52.0, null)), "locationValid");
        RequestAssertions.rejectedFor(
                validator.validate(RequestFixtures.location(null, 91.0, 21.0)), "latitude");
        RequestAssertions.rejectedFor(
                validator.validate(RequestFixtures.location(null, 52.0, 181.0)), "longitude");
    }
}

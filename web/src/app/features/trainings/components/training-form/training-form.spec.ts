import { configureFormTests, Fixtures } from './testing/form-fixtures';
import { Assertions } from './testing/form-assertions';

describe('TrainingForm', () => {
  beforeEach(configureFormTests);

  it('creates the form', () => {
    const fixture = Fixtures.emptyForm();
    fixture.fixture.detectChanges();
    Assertions.created(fixture);
  });

  it('selects notification by default and asks for confirmation', () => {
    const fixture = Fixtures.validFormWithDefaultNotification();
    fixture.component.submit();
    Assertions.notificationConfirmationRequired(fixture);
  });

  it('sends the selected notification only after confirmation', () => {
    const fixture = Fixtures.validFormWithDefaultNotification();
    fixture.component.submit();
    fixture.component.emitTraining();
    Assertions.notificationConfirmed(fixture);
  });

  it('rejects a past date and non-positive capacity', () => {
    const fixture = Fixtures.pastTraining();
    fixture.component.submit();
    Assertions.dateAndCapacityRejected(fixture);
  });

  it.each([null, 0, -1, 10.001, 100000000])('blocks invalid paid amount %s', (price) => {
    const fixture = Fixtures.paidForm(price);
    fixture.component.submit();
    Assertions.invalidPriceRejected(fixture);
  });

  it('accepts a valid paid amount', () => {
    const fixture = Fixtures.paidForm(80.5);
    fixture.component.submit();
    Assertions.validPriceSubmitted(fixture, 80.5);
  });

  it.each(['FREE', 'TO_BE_AGREED'] as const)('clears the amount when switching to %s', (mode) => {
    const fixture = Fixtures.paidForm(80);
    fixture.component.form.controls.priceMode.setValue(mode);

    fixture.component.priceModeChanged();
    fixture.component.submit();
    Assertions.priceCleared(fixture, mode);
  });

  it('disables exams and prefills the trainer', () => {
    const fixture = Fixtures.emptyForm();
    fixture.fixture.detectChanges();
    Assertions.examDisabledAndTrainerPrefilled(fixture);
  });

  it('preserves price and description when editing', () => {
    const fixture = Fixtures.editingPaidTraining();
    fixture.component.submit();
    Assertions.editedDetailsPreserved(fixture);
  });

  it('submits training without dictionaries', () => {
    const fixture = Fixtures.validForm();
    fixture.component.submit();
    Assertions.submittedWithoutDictionaries(fixture);
  });

  it('allows omitting description, image and capacity', () => {
    const fixture = Fixtures.validForm();
    fixture.component.submit();
    Assertions.optionalDetailsOmitted(fixture);
  });

  it.each([0, -1, 1.5])('blocks invalid capacity %s', (capacity) => {
    const fixture = Fixtures.capacity(capacity);
    fixture.component.submit();
    Assertions.submissionBlocked(fixture);
  });

  it('rejects an oversized description without losing the text', () => {
    const fixture = Fixtures.description(2001);
    fixture.component.submit();
    Assertions.longDescriptionPreserved(fixture, 2001);
  });

  it('uses the school context without showing a selector', () => {
    const fixture = Fixtures.school(7);
    fixture.fixture.detectChanges();
    Assertions.schoolSelectedWithoutDropdown(fixture, 7);
  });

  it('rejects unsupported images and recovers after selecting a valid image', () => {
    const fixture = Fixtures.validForm();
    const invalidImage = Fixtures.imageSelection('image/svg+xml');

    fixture.component.selectImage(invalidImage.event);
    fixture.component.submit();
    Assertions.invalidImageRejected(fixture);

    const validImage = Fixtures.imageSelection('image/png');
    fixture.component.selectImage(validImage.event);
    fixture.component.submit();
    Assertions.validImageSubmitted(fixture, validImage.file);
  });
});

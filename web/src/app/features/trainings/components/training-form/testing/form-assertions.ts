import { expect } from 'vitest';
import { FormFixture } from './form-fixtures';

export const Assertions = {
  created(s: FormFixture) {
    expect(s.component).toBeTruthy();
  },
  dateAndCapacityRejected(s: FormFixture) {
    expect(s.component.form.controls.startAt.hasError('futureDate')).toBe(true);
    expect(s.component.form.controls.capacity.hasError('min')).toBe(true);
    expect(s.submitted).not.toHaveBeenCalled();
  },
  invalidPriceRejected(s: FormFixture) {
    expect(s.submitted).not.toHaveBeenCalled();
    expect(s.component.form.controls.price.invalid).toBe(true);
    expect(s.component.form.controls.price.touched).toBe(true);
  },
  validPriceSubmitted(s: FormFixture, price: number) {
    expect(s.component.form.controls.price.valid).toBe(true);
    expect(s.submitted).toHaveBeenCalledWith({
      training: expect.objectContaining({ priceMode: 'PAID', price }),
      image: null,
    });
  },
  priceCleared(s: FormFixture, mode: string) {
    expect(s.component.form.controls.price.value).toBeNull();
    expect(s.component.form.controls.price.valid).toBe(true);
    expect(s.submitted).toHaveBeenCalledWith({
      training: expect.objectContaining({ priceMode: mode, price: null }),
      image: null,
    });
  },
  examDisabledAndTrainerPrefilled(s: FormFixture) {
    const buttons = Array.from(
      s.fixture.nativeElement.querySelectorAll('button'),
    ) as HTMLButtonElement[];
    expect(buttons.find((button) => button.textContent?.includes('Egzamin'))?.disabled).toBe(true);
    expect(s.component.form.controls.trainerName.value).toBe('Anna');
  },
  editedDetailsPreserved(s: FormFixture) {
    expect(s.component.form.controls.price.value).toBe(80.5);
    expect(s.component.form.controls.trainerName.value).toBe('Joanna');
    expect(s.submitted).toHaveBeenCalledOnce();
    expect(s.submitted.mock.calls[0][0].training).toMatchObject({
      schoolId: 7,
      description: 'Zabierz smaczki',
      priceMode: 'PAID',
      price: 80.5,
    });
  },
  submittedWithoutDictionaries(s: FormFixture) {
    expect(s.submitted).toHaveBeenCalledOnce();
    expect(s.submitted.mock.calls[0][0].training.eventKind).toBe('TRAINING');
    expect(s.submitted.mock.calls[0][0].training).not.toHaveProperty('trainingTypeId');
  },
  optionalDetailsOmitted(s: FormFixture) {
    expect(s.submitted).toHaveBeenCalledWith({
      training: expect.objectContaining({
        description: '',
        capacity: null,
        priceMode: 'TO_BE_AGREED',
        price: null,
      }),
      image: null,
    });
  },
  submissionBlocked(s: FormFixture) {
    expect(s.submitted).not.toHaveBeenCalled();
  },
  longDescriptionPreserved(s: FormFixture, length: number) {
    expect(s.submitted).not.toHaveBeenCalled();
    expect(s.component.form.controls.description.value).toHaveLength(length);
  },
  schoolSelectedWithoutDropdown(s: FormFixture, id: number) {
    expect(s.component.form.controls.schoolId.value).toBe(id);
    expect(s.fixture.nativeElement.querySelector('[formControlName="schoolId"]')).toBeNull();
  },
  invalidImageRejected(s: FormFixture) {
    expect(s.submitted).not.toHaveBeenCalled();
    expect(s.component.imageError).toBeTruthy();
  },
  validImageSubmitted(s: FormFixture, image: File) {
    expect(s.component.imageError).toBeNull();
    expect(s.submitted).toHaveBeenCalledWith({ training: expect.any(Object), image });
  },
};

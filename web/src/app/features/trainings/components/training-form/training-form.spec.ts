import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrainingForm } from './training-form';

describe('TrainingForm', () => {
  let component: TrainingForm;
  let fixture: ComponentFixture<TrainingForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TrainingForm],
    }).compileComponents();

    fixture = TestBed.createComponent(TrainingForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('rejects a past date and non-positive capacity', () => {
    component.form.patchValue({
      title: 'Obedience',
      trainerName: 'Jan',
      startAt: '2020-01-01T10:00',
      capacity: 0,
    });

    expect(component.form.controls.startAt.hasError('futureDate')).toBe(true);
    expect(component.form.controls.capacity.hasError('min')).toBe(true);
  });

  it('submits valid training data', () => {
    const emit = vi.spyOn(component.trainingSubmitted, 'emit');
    component.form.setValue({
      schoolId: 1,
      title: 'Basic obedience',
      trainerName: 'Jan Kowalski',
      level: 'BASIC',
      startAt: '2099-01-01T10:00',
      capacity: 10,
    });

    component.submit();

    expect(emit).toHaveBeenCalledOnce();
  });
});

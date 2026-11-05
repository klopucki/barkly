import { TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { of } from 'rxjs';
import { TrainingForm } from '../training-form';
import { AuthService } from '../../../../auth/auth.service';
import { SchoolService } from '../../../../schools/school.service';
import { Training } from '../../../training.model';

export async function configureFormTests(): Promise<void> {
  await TestBed.configureTestingModule({
    imports: [TrainingForm],
    providers: [
      { provide: AuthService, useValue: { currentUser: () => ({ displayName: 'Anna' }) } },
      { provide: SchoolService, useValue: { mine$: () => of([]) } },
    ],
  }).compileComponents();
}

function create(training?: Training, schoolId?: number) {
  const fixture = TestBed.createComponent(TrainingForm);
  if (training) fixture.componentRef.setInput('training', training);
  if (schoolId !== undefined) fixture.componentRef.setInput('schoolId', schoolId);
  fixture.detectChanges();
  const component = fixture.componentInstance;
  return { fixture, component, submitted: vi.spyOn(component.trainingSubmitted, 'emit') };
}
export type FormFixture = ReturnType<typeof create>;

function valid(notifySchool = false) {
  const scenario = create();
  scenario.component.form.patchValue({
    notifySchool,
    schoolId: 1,
    title: 'Trening grupy',
    trainerName: 'Anna',
    startAt: '2099-01-01T10:00',
  });
  return scenario;
}
function paid(price: number | null) {
  const scenario = valid();
  scenario.component.form.controls.priceMode.setValue('PAID');
  scenario.component.priceModeChanged();
  scenario.component.form.controls.price.setValue(price);
  return scenario;
}
export const Fixtures = {
  imageSelection(type: string) {
    const file = new File(['image'], 'image', { type });
    const event = { target: { files: [file], value: 'image' } } as unknown as Event;
    return { file, event };
  },
  emptyForm: () => create(),
  validForm: valid,
  validFormWithDefaultNotification() {
    const scenario = create();
    scenario.component.form.patchValue({
      schoolId: 1,
      title: 'Trening grupy',
      trainerName: 'Anna',
      startAt: '2099-01-01T10:00',
    });
    return scenario;
  },
  paidForm: paid,
  capacity(capacity: number) {
    const scenario = valid();
    scenario.component.form.controls.capacity.setValue(capacity);
    return scenario;
  },
  pastTraining() {
    const scenario = valid();
    scenario.component.form.patchValue({ startAt: '2020-01-01T10:00', capacity: 0 });
    return scenario;
  },
  description(length: number) {
    const scenario = valid();
    scenario.component.form.controls.description.setValue('a'.repeat(length));
    return scenario;
  },
  school: (schoolId: number) => create(undefined, schoolId),
  editingPaidTraining() {
    return create({
      id: 1,
      schoolId: 7,
      title: 'Trening grupy',
      trainerName: 'Joanna',
      startAt: '2099-01-01T10:00:00',
      capacity: 5,
      bookedCount: 2,
      description: 'Zabierz smaczki',
      eventKind: 'TRAINING',
      priceMode: 'PAID',
      price: 80.5,
      imageKey: null,
      pawCount: 0,
      pawedByCurrentUser: false,
    });
  },
};

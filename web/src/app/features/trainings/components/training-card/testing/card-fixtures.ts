import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { TrainingCard } from '../training-card';
import { AuthService } from '../../../../auth/auth.service';

export async function configureCardTests(): Promise<void> {
  await TestBed.configureTestingModule({
    imports: [TrainingCard],
    providers: [
      provideRouter([]),
      { provide: AuthService, useValue: { currentUser: signal(null) } },
    ],
  }).compileComponents();
}
export const Fixtures = {
  trainingCard() {
    const fixture = TestBed.createComponent(TrainingCard);
    fixture.componentRef.setInput('training', {
      id: 1,
      schoolId: 1,
      title: 'Posłuszeństwo dla początkujących',
      trainerName: 'Jan Kowalski',
      startAt: '2099-01-01T10:00:00',
      capacity: 10,
      bookedCount: 3,
      imageKey: null,
      pawCount: 0,
      pawedByCurrentUser: false,
    });
    return fixture;
  },
};
export type CardFixture = ReturnType<typeof Fixtures.trainingCard>;

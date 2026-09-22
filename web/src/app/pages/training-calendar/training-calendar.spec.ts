import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { TrainingService } from '../../features/trainings/training.service';
import { TrainingCalendar } from './training-calendar';

describe('TrainingCalendar', () => {
  let fixture: ComponentFixture<TrainingCalendar>;

  beforeEach(async () => {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    tomorrow.setHours(18, 0, 0, 0);

    await TestBed.configureTestingModule({
      imports: [TrainingCalendar],
      providers: [
        provideRouter([]),
        {
          provide: TrainingService,
          useValue: {
            getMyBookings$: () =>
              of([
                {
                  bookingId: 1,
                  trainingId: 2,
                  dogId: 3,
                  dogName: 'Luna',
                  trainingTitle: 'Posłuszeństwo',
                  trainerName: 'Jan Kowalski',
                  trainingType: 'Grupowy',
                  startAt: tomorrow.toISOString(),
                },
              ]),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TrainingCalendar);
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('shows the user’s upcoming booked training', () => {
    expect(fixture.nativeElement.textContent).toContain('Posłuszeństwo');
    expect(fixture.nativeElement.textContent).toContain('Luna');
  });
});

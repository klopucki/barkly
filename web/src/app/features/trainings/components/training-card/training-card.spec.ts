import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { signal } from '@angular/core';

import { TrainingCard } from './training-card';
import { AuthService } from '../../../auth/auth.service';

describe('TrainingCard', () => {
  let component: TrainingCard;
  let fixture: ComponentFixture<TrainingCard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TrainingCard],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: { currentUser: signal(null) } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TrainingCard);
    fixture.componentRef.setInput('training', {
      id: 1,
      schoolId: 1,
      title: 'Posłuszeństwo dla początkujących',
      trainerName: 'Jan Kowalski',
      trainingType: { id: 1, code: 'GROUP', name: 'Grupowy' },
      trainingLevel: null,
      targetGroup: null,
      homeVisit: false,
      startAt: '2099-01-01T10:00:00',
      capacity: 10,
      bookedCount: 3,
      imageKey: null,
      pawCount: 0,
      pawedByCurrentUser: false,
    });
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('shows the remaining capacity on the card', () => {
    expect(fixture.nativeElement.textContent).toContain('7 wolnych z 10');
  });
});

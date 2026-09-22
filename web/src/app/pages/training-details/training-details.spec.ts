import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { signal } from '@angular/core';
import { of } from 'rxjs';

import { TrainingDetails } from './training-details';
import { TrainingService } from '../../features/trainings/training.service';
import { AuthService } from '../../features/auth/auth.service';

describe('TrainingDetails', () => {
  let component: TrainingDetails;
  let fixture: ComponentFixture<TrainingDetails>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TrainingDetails],
      providers: [
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => '1' } } } },
        {
          provide: TrainingService,
          useValue: {
            getTrainingById$: () => of(null),
            getBookingsForTraining$: () => of([]),
          },
        },
        { provide: AuthService, useValue: { currentUser: signal(null) } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TrainingDetails);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrainingCard } from './training-card';

describe('TrainingCard', () => {
  let component: TrainingCard;
  let fixture: ComponentFixture<TrainingCard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TrainingCard],
    }).compileComponents();

    fixture = TestBed.createComponent(TrainingCard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

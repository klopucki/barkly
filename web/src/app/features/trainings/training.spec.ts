import { TestBed } from '@angular/core/testing';

import { TrainingService } from './training.service';
import { provideHttpClient } from '@angular/common/http';

describe('Training', () => {
  let service: TrainingService;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient()] });
    service = TestBed.inject(TrainingService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});

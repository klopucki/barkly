import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { Trainings } from '../trainings';
import { TrainingService } from '../../../features/trainings/training.service';
import { AuthService } from '../../../features/auth/auth.service';
import { FavoritesService } from '../../../shared/favorites.service';
import { Training } from '../../../features/trainings/training.model';

function training(id: number, day: string): Training {
  return {
    id,
    schoolId: 1,
    title: 'Trening',
    trainerName: 'Anna',
    startAt: `2099-01-${day}T10:00:00`,
    capacity: null,
    bookedCount: 0,
    imageKey: null,
    pawCount: 0,
    pawedByCurrentUser: false,
  };
}

export async function trainingListFixture(action: 'add' | 'edit', day: string) {
  const existing = [training(1, '03'), training(2, '06')];
  const saved = training(action === 'add' ? 3 : 2, day);
  const ordered = [...existing.filter((item) => item.id !== saved.id), saved].sort((a, b) =>
    a.startAt.localeCompare(b.startAt),
  );
  const api = {
    addTraining$: vi.fn(() => of(saved)),
    updateTraining$: vi.fn(() => of(saved)),
    search$: vi.fn(() =>
      of({ items: ordered, page: 0, totalPages: 1, totalElements: ordered.length, size: 20 }),
    ),
  };
  await TestBed.configureTestingModule({
    imports: [Trainings],
    providers: [
      { provide: TrainingService, useValue: api },
      { provide: AuthService, useValue: { currentUser: () => null } },
      { provide: FavoritesService, useValue: { has: () => false } },
    ],
  }).compileComponents();
  const component = TestBed.createComponent(Trainings).componentInstance;
  component.trainingsFromApi.set(existing);
  component.page.set(2);
  if (action === 'edit') component.openEditTraining(existing[1]);
  else component.openAddTraining();
  return { component, api, submission: { training: saved, image: null } };
}

export type TrainingListFixture = Awaited<ReturnType<typeof trainingListFixture>>;

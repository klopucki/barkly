import { Component, computed, inject, signal, effect } from '@angular/core';
import { TrainingService } from '../../features/trainings/training.service';
import { TrainingCard } from '../../features/trainings/components/training-card/training-card';
import { Training } from '../../features/trainings/training.model';
import {
  TrainingForm,
  TrainingFormValue,
} from '../../features/trainings/components/training-form/training-form';
import { Modal } from '../../shared/components/modal/modal';
import { toSignal } from '@angular/core/rxjs-interop';
import { tap } from 'rxjs';

@Component({
  selector: 'app-trainings',
  imports: [TrainingCard, TrainingForm, Modal],
  templateUrl: './trainings.html',
})
export class Trainings {
  private readonly trainingService = inject(TrainingService);

  searchText = signal('');

  trainingsFromApi = toSignal(
    this.trainingService.getTrainings$().pipe(tap(() => this.isLoading.set(false))),
    { initialValue: [] },
  );

  localTrainings = signal<Training[]>([]);
  isLoading = signal(true);

  trainings = computed(() => {
    const search = this.searchText().trim().toLowerCase();

    const trainings = [...this.trainingsFromApi(), ...this.localTrainings()];

    if (!search) {
      return trainings;
    }

    return trainings.filter(
      (training) =>
        training.title.toLowerCase().includes(search) ||
        training.trainerName.toLowerCase().includes(search) ||
        training.level.toLowerCase().includes(search),
    );
  });

  isAddTrainingOpen = signal(false);

  constructor() {
    effect(() => {
      localStorage.setItem('training-search', this.searchText());
    });
  }

  openAddTraining(): void {
    this.isAddTrainingOpen.set(true);
  }

  closeAddTraining(): void {
    this.isAddTrainingOpen.set(false);
  }

  addTraining(training: TrainingFormValue): void {
    this.trainingService.addTraining$(training).subscribe((savedTraining) => {
      this.localTrainings.update((trainings) => [...trainings, savedTraining]);
      this.closeAddTraining();
    });
  }
}

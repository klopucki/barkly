import { Component, computed, inject, signal, effect } from '@angular/core';
import { TrainingService } from '../../features/trainings/training';
import { TrainingCard } from '../../features/trainings/components/training-card/training-card';
import {
  TrainingForm,
  TrainingFormValue,
} from '../../features/trainings/components/training-form/training-form';

@Component({
  selector: 'app-trainings',
  imports: [TrainingCard, TrainingForm],
  templateUrl: './trainings.html',
})
export class Trainings {
  private readonly trainingService = inject(TrainingService);

  searchText = signal('');

  trainings = computed(() => {
    const search = this.searchText().trim().toLowerCase();

    if (!search) {
      return this.trainingService.trainings();
    }

    return this.trainingService
      .trainings()
      .filter(
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
    this.trainingService.addTraining(training);
    this.isAddTrainingOpen.set(false);
  }
}
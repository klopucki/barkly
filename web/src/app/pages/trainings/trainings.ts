import { Component, computed, inject, signal, effect, OnInit } from '@angular/core';
import { TrainingService } from '../../features/trainings/training.service';
import { TrainingCard } from '../../features/trainings/components/training-card/training-card';
import { Training } from '../../features/trainings/training.model';
import {
  TrainingForm,
  TrainingFormValue,
} from '../../features/trainings/components/training-form/training-form';
import { Modal } from '../../shared/components/modal/modal';

@Component({
  selector: 'app-trainings',
  imports: [TrainingCard, TrainingForm, Modal],
  templateUrl: './trainings.html',
})
export class Trainings implements OnInit {
  private readonly trainingService = inject(TrainingService);

  searchText = signal('');
  trainingsFromApi = signal<Training[]>([]);
  isLoading = signal(true);
  isAddTrainingOpen = signal(false);

  trainings = computed(() => {
    const search = this.searchText().trim().toLowerCase();
    const trainings = this.trainingsFromApi();

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

  constructor() {
    effect(() => {
      localStorage.setItem('training-search', this.searchText());
    });
  }

  ngOnInit(): void {
    this.loadTrainings();
  }

  loadTrainings(): void {
    this.isLoading.set(true);

    this.trainingService.getTrainings$().subscribe({
      next: (trainings) => {
        this.trainingsFromApi.set(trainings);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      },
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
      this.trainingsFromApi.update((trainings) => [...trainings, savedTraining]);
      this.closeAddTraining();
    });
  }

  deleteTraining(id: number): void {
    this.trainingService.deleteTraining(id).subscribe(() => {
      this.trainingsFromApi.update((trainings) =>
        trainings.filter((training) => training.id !== id),
      );
    });
  }
}

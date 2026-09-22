import { Component, computed, inject, signal, effect, OnInit } from '@angular/core';
import { TrainingService } from '../../features/trainings/training.service';
import { TrainingCard } from '../../features/trainings/components/training-card/training-card';
import { Training } from '../../features/trainings/training.model';
import {
  TrainingForm,
  TrainingFormSubmission,
} from '../../features/trainings/components/training-form/training-form';
import { Modal } from '../../shared/components/modal/modal';
import { AuthService } from '../../features/auth/auth.service';
import { FavoritesService } from '../../shared/favorites.service';

@Component({
  selector: 'app-trainings',
  imports: [TrainingCard, TrainingForm, Modal],
  templateUrl: './trainings.html',
  styleUrl: './trainings.css',
})
export class Trainings implements OnInit {
  private readonly trainingService = inject(TrainingService);
  protected readonly auth = inject(AuthService);
  protected readonly favorites = inject(FavoritesService);

  searchText = signal('');
  typeFilter = signal('');
  favoritesOnly = signal(false);
  trainingsFromApi = signal<Training[]>([]);
  page = signal(0);
  totalPages = signal(0);
  isLoading = signal(true);
  isAddTrainingOpen = signal(false);
  addTrainingError = signal<string | null>(null);
  editingTraining = signal<Training | null>(null);
  private searchTimer?: ReturnType<typeof setTimeout>;

  trainings = computed(() => {
    const search = this.searchText().trim().toLowerCase();
    const type = this.typeFilter();
    const trainings = this.trainingsFromApi();

    return trainings.filter(
      (training) =>
        (!search ||
          training.title.toLowerCase().includes(search) ||
          training.trainerName.toLowerCase().includes(search) ||
          training.trainingType.name.toLowerCase().includes(search) ||
          training.trainingLevel?.name.toLowerCase().includes(search) ||
          training.targetGroup?.name.toLowerCase().includes(search)) &&
        (!type || training.trainingType.name === type) &&
        (!this.favoritesOnly() || this.favorites.has('training', training.id)),
    );
  });

  trainingTypes = computed(() => [
    ...new Set(this.trainingsFromApi().map((training) => training.trainingType.name)),
  ]);

  constructor() {
    effect(() => {
      localStorage.setItem('training-search', this.searchText());
    });
  }

  ngOnInit(): void {
    this.loadTrainings();
  }

  loadTrainings(): void {
    this.loadPage(0);
  }

  search(query: string): void {
    this.searchText.set(query);
    clearTimeout(this.searchTimer);
    this.searchTimer = setTimeout(() => this.loadPage(0), 250);
  }

  setType(type: string): void {
    this.typeFilter.set(type);
    this.loadPage(0);
  }

  loadMore(): void {
    this.loadPage(this.page() + 1, true);
  }

  toggleFavorites(): void {
    if (!this.auth.currentUser()) return;
    this.favoritesOnly.set(!this.favoritesOnly());
    this.loadPage(0);
  }

  private loadPage(page: number, append = false): void {
    this.isLoading.set(true);

    this.trainingService
      .search$(this.searchText(), this.typeFilter(), page, 20, this.favoritesOnly())
      .subscribe({
        next: (result) => {
          this.trainingsFromApi.set(
            append ? [...this.trainingsFromApi(), ...result.items] : result.items,
          );
          this.page.set(result.page);
          this.totalPages.set(result.totalPages);
          this.isLoading.set(false);
        },
        error: () => {
          this.isLoading.set(false);
        },
      });
  }

  openAddTraining(): void {
    this.addTrainingError.set(null);
    this.editingTraining.set(null);
    this.isAddTrainingOpen.set(true);
  }

  openEditTraining(training: Training): void {
    this.addTrainingError.set(null);
    this.editingTraining.set(training);
    this.isAddTrainingOpen.set(true);
  }

  closeAddTraining(): void {
    this.isAddTrainingOpen.set(false);
    this.editingTraining.set(null);
  }

  saveTraining(submission: TrainingFormSubmission): void {
    const training = this.editingTraining();
    if (training) {
      this.updateTraining(training.id, submission);
      return;
    }
    this.addTraining(submission);
  }

  private updateTraining(id: number, submission: TrainingFormSubmission): void {
    this.addTrainingError.set(null);
    this.trainingService.updateTraining$(id, submission.training).subscribe({
      next: (updated) => {
        if (!submission.image) {
          this.finishUpdatingTraining(updated);
          return;
        }
        this.trainingService.uploadTrainingImage$(id, submission.image).subscribe({
          next: (withImage) => this.finishUpdatingTraining(withImage),
          error: (error) => {
            this.finishUpdatingTraining(updated);
            const message = error.error?.image ?? error.error?.message;
            this.addTrainingError.set(
              message
                ? `Trening został zapisany, ale zdjęcie nie zostało przesłane: ${message}`
                : 'Trening został zapisany, ale zdjęcie nie zostało przesłane.',
            );
          },
        });
      },
      error: (error) => {
        this.addTrainingError.set(error.error?.message ?? 'Could not update training.');
      },
    });
  }

  private finishUpdatingTraining(updated: Training): void {
    this.trainingsFromApi.update((trainings) =>
      trainings.map((training) => (training.id === updated.id ? updated : training)),
    );
    this.closeAddTraining();
  }

  addTraining(submission: TrainingFormSubmission): void {
    this.addTrainingError.set(null);
    this.trainingService.addTraining$(submission.training).subscribe({
      next: (savedTraining) => {
        if (!submission.image) {
          this.finishAddingTraining(savedTraining);
          return;
        }

        this.trainingService.uploadTrainingImage$(savedTraining.id, submission.image).subscribe({
          next: (trainingWithImage) => this.finishAddingTraining(trainingWithImage),
          error: (error) => {
            const message = error.error?.image ?? error.error?.message;
            this.finishAddingTraining(savedTraining);
            this.addTrainingError.set(
              message
                ? `Training was saved without an image: ${message}`
                : 'Training was saved, but the image could not be uploaded.',
            );
          },
        });
      },
      error: (error) => {
        const message = error.error?.message;
        this.addTrainingError.set(message ?? 'Could not save the training.');
      },
    });
  }

  private finishAddingTraining(savedTraining: Training): void {
    this.trainingsFromApi.update((trainings) => [...trainings, savedTraining]);
    this.closeAddTraining();
  }

  deleteTraining(id: number): void {
    this.trainingService.deleteTraining(id).subscribe(() => {
      this.trainingsFromApi.update((trainings) =>
        trainings.filter((training) => training.id !== id),
      );
    });
  }
}

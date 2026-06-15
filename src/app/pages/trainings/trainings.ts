import { Component, inject } from '@angular/core';
import { Training } from '../../features/trainings/training.model';
import { TrainingCard } from '../../features/trainings/components/training-card/training-card';
import { TrainingService } from '../../features/trainings/training';

@Component({
  selector: 'app-trainings',
  imports: [TrainingCard],
  templateUrl: './trainings.html',
  styleUrl: './trainings.css',
})
export class Trainings {
  private readonly trainingService = inject(TrainingService);

  trainings = this.trainingService.trainings;

  onBookTraining(training: Training): void {
    this.trainingService.bookTraining(training.id);
  }
}

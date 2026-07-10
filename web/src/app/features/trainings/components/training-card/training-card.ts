import { Component, input, output } from '@angular/core';
import { Training, trainingImageUrl } from '../../training.model';
import { RouterLink } from '@angular/router';
import { TrainingLevelPipe } from '../../../../shared/pipes/training-level-pipe';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-training-card',
  imports: [RouterLink, TrainingLevelPipe, DatePipe],
  templateUrl: './training-card.html',
})
export class TrainingCard {
  protected readonly trainingImageUrl = trainingImageUrl;

  training = input.required<Training>();

  deleteClicked = output<number>();
}

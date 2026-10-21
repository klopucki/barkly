import { Component, inject, input, output } from '@angular/core';
import { Training, trainingImageUrl } from '../../training.model';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { AuthService } from '../../../auth/auth.service';

@Component({
  selector: 'app-training-card',
  imports: [DatePipe, RouterLink],
  templateUrl: './training-card.html',
  styleUrl: './training-card.css',
})
export class TrainingCard {
  protected readonly auth = inject(AuthService);
  protected readonly trainingImageUrl = trainingImageUrl;

  training = input.required<Training>();
  favorite = input(false);

  deleteClicked = output<number>();
  editClicked = output<Training>();
  favoriteClicked = output<Training>();

  protected isCapacityLow(): boolean {
    const { capacity, bookedCount } = this.training();
    return capacity !== null && capacity - bookedCount <= 2;
  }

  protected isPast(): boolean {
    return new Date(this.training().startAt) <= new Date();
  }

  protected capacityLabel(): string {
    const { capacity, bookedCount } = this.training();
    return capacity === null
      ? 'Bez limitu miejsc'
      : capacity - bookedCount <= 0
        ? 'Brak wolnych miejsc'
        : `Wolne miejsca: ${capacity - bookedCount} z ${capacity}`;
  }
}

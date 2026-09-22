import { Component, inject, input, output } from '@angular/core';
import { Training, trainingImageUrl } from '../../training.model';
import { Router } from '@angular/router';
import { DatePipe } from '@angular/common';
import { AuthService } from '../../../auth/auth.service';

@Component({
  selector: 'app-training-card',
  imports: [DatePipe],
  templateUrl: './training-card.html',
  styleUrl: './training-card.css',
})
export class TrainingCard {
  protected readonly auth = inject(AuthService);
  protected readonly trainingImageUrl = trainingImageUrl;
  private readonly router = inject(Router);

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
      ? 'bez limitu miejsc'
      : `${Math.max(0, capacity - bookedCount)} wolnych z ${capacity}`;
  }

  protected openDetails(event: Event): void {
    const target = event.target as HTMLElement;
    if (target.closest('button, a, input, label')) return;
    event.preventDefault();
    this.router.navigate(['/trainings', this.training().id]);
  }
}

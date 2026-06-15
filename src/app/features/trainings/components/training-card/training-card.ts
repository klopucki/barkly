import { Component, input, output } from '@angular/core';
import { Training } from '../../training.model';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-training-card',
  imports: [RouterLink],
  templateUrl: './training-card.html',
})
export class TrainingCard {
  training = input.required<Training>();

  bookClicked = output<Training>();

  onBookClick(): void {
    this.bookClicked.emit(this.training());
  }
}

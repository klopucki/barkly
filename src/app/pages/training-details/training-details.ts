import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TrainingService } from '../../features/trainings/training';
import { FormBuilder, Validators } from '@angular/forms';
import {
  BookingForm,
  BookingFormValue,
} from '../../features/bookings/components/booking-form/booking-form';

@Component({
  selector: 'app-training-details',
  imports: [BookingForm],
  templateUrl: './training-details.html',
})
export class TrainingDetails {
  private readonly route = inject(ActivatedRoute);
  private readonly trainingService = inject(TrainingService);

  trainingId = signal(Number(this.route.snapshot.paramMap.get('id')));

  training = computed(() => this.trainingService.getTrainingById(this.trainingId()));

  bookings = computed(() => this.trainingService.getBookingsForTraining(this.trainingId()));

  submitBooking(booking: BookingFormValue): void {
    this.trainingService.createBooking(this.trainingId(), booking);
  }
}

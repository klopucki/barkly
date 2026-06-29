import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TrainingService } from '../../features/trainings/training.service';
import { Modal } from '../../shared/components/modal/modal';

import {
  BookingForm,
  BookingFormValue,
} from '../../features/bookings/components/booking-form/booking-form';
import { toSignal } from '@angular/core/rxjs-interop';
import { Booking } from '../../features/bookings/components/booking-form/booking.model';

@Component({
  selector: 'app-training-details',
  imports: [BookingForm, Modal],
  templateUrl: './training-details.html',
})
export class TrainingDetails implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly trainingService = inject(TrainingService);

  trainingId = signal(Number(this.route.snapshot.paramMap.get('id')));

  training = toSignal(this.trainingService.getTrainingById$(this.trainingId()), {
    initialValue: null,
  });

  bookings = signal<Booking[]>([]);

  ngOnInit(): void {
    this.loadBookings();
  }

  loadBookings(): void {
    this.trainingService
      .getBookingsForTraining$(this.trainingId())
      .subscribe((bookings) => this.bookings.set(bookings));
  }

  isBookingModalOpen = signal(false);

  openBookingModal(): void {
    this.isBookingModalOpen.set(true);
  }

  closeBookingModal(): void {
    this.isBookingModalOpen.set(false);
  }

  submitBooking(booking: BookingFormValue): void {
    this.trainingService.createBooking$(this.trainingId(), booking).subscribe((savedBooking) => {
      this.bookings.update((bookings) => [...bookings, savedBooking]);
      this.closeBookingModal();
    });
  }
}

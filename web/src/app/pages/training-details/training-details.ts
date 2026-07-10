import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TrainingService } from '../../features/trainings/training.service';
import { Modal } from '../../shared/components/modal/modal';
import {
  BookingForm,
  BookingFormValue,
} from '../../features/bookings/components/booking-form/booking-form';
import { Booking } from '../../features/bookings/components/booking-form/booking.model';
import { Training, trainingImageUrl } from '../../features/trainings/training.model';

@Component({
  selector: 'app-training-details',
  imports: [BookingForm, Modal],
  templateUrl: './training-details.html',
})
export class TrainingDetails implements OnInit {
  protected readonly trainingImageUrl = trainingImageUrl;

  private readonly route = inject(ActivatedRoute);
  private readonly trainingService = inject(TrainingService);

  trainingId = signal(Number(this.route.snapshot.paramMap.get('id')));

  training = signal<Training | null>(null);
  bookings = signal<Booking[]>([]);
  isBookingModalOpen = signal(false);
  imageUploadError = signal<string | null>(null);

  ngOnInit(): void {
    this.loadTraining();
    this.loadBookings();
  }

  loadTraining(): void {
    this.trainingService
      .getTrainingById$(this.trainingId())
      .subscribe((training) => this.training.set(training));
  }

  loadBookings(): void {
    this.trainingService
      .getBookingsForTraining$(this.trainingId())
      .subscribe((bookings) => this.bookings.set(bookings));
  }

  openBookingModal(): void {
    this.isBookingModalOpen.set(true);
  }

  closeBookingModal(): void {
    this.isBookingModalOpen.set(false);
  }

  submitBooking(booking: BookingFormValue): void {
    this.trainingService.createBooking$(this.trainingId(), booking).subscribe((savedBooking) => {
      this.bookings.update((bookings) => [...bookings, savedBooking]);

      this.training.update((training) => {
        if (!training) {
          return training;
        }

        return {
          ...training,
          bookedCount: training.bookedCount + 1,
        };
      });

      this.closeBookingModal();
    });
  }

  deleteBooking(id: number): void {
    this.trainingService.deleteBooking$(id).subscribe(() => {
      this.bookings.update((bookings) => bookings.filter((booking) => booking.id !== id));

      this.training.update((training) => {
        if (!training) {
          return training;
        }

        return {
          ...training,
          bookedCount: Math.max(0, training.bookedCount - 1),
        };
      });
    });
  }

  uploadImage(event: Event): void {
    const input = event.target as HTMLInputElement;
    const image = input.files?.[0];
    if (!image) {
      return;
    }

    this.imageUploadError.set(null);
    this.trainingService.uploadTrainingImage$(this.trainingId(), image).subscribe({
      next: (training) => {
        this.training.set(training);
        input.value = '';
      },
      error: (error) => {
        this.imageUploadError.set(
          error.error?.image ?? error.error?.message ?? 'Could not upload the image.',
        );
        input.value = '';
      },
    });
  }
}

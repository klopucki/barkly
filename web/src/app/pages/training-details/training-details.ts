import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DatePipe } from '@angular/common';
import { TrainingService } from '../../features/trainings/training.service';
import { Modal } from '../../shared/components/modal/modal';
import {
  BookingForm,
  BookingFormValue,
} from '../../features/bookings/components/booking-form/booking-form';
import { Booking } from '../../features/bookings/components/booking-form/booking.model';
import { Training, trainingImageUrl } from '../../features/trainings/training.model';
import { AuthService } from '../../features/auth/auth.service';
import { DogService } from '../../features/dogs/dog.service';
import { Dog } from '../../features/dogs/dog.model';
import {
  TrainingForm,
  TrainingFormSubmission,
} from '../../features/trainings/components/training-form/training-form';

@Component({
  selector: 'app-training-details',
  imports: [BookingForm, TrainingForm, Modal, DatePipe],
  templateUrl: './training-details.html',
  styleUrl: './training-details.css',
})
export class TrainingDetails implements OnInit {
  protected readonly trainingImageUrl = trainingImageUrl;

  private readonly route = inject(ActivatedRoute);
  private readonly trainingService = inject(TrainingService);
  protected readonly auth = inject(AuthService);
  private readonly dogsApi = inject(DogService);

  trainingId = signal(Number(this.route.snapshot.paramMap.get('id')));

  training = signal<Training | null>(null);
  bookings = signal<Booking[]>([]);
  isBookingModalOpen = signal(false);
  imageUploadError = signal<string | null>(null);
  isEditModalOpen = signal(false);
  isQuickBookingModalOpen = signal(false);
  dogs = signal<Dog[]>([]);
  bookingError = signal<string | null>(null);
  quickBookingDogId = signal<number | null>(null);

  ngOnInit(): void {
    this.loadTraining();
    this.loadBookings();
    if (this.auth.currentUser()) this.dogsApi.mine$().subscribe((dogs) => this.dogs.set(dogs));
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

  enrolledDogIds(): Set<number> {
    return new Set(
      this.bookings()
        .map((booking) => booking.dogId)
        .filter((id): id is number => id !== null),
    );
  }

  hasAvailableDog(): boolean {
    const enrolled = this.enrolledDogIds();
    return this.dogs().some((dog) => !enrolled.has(dog.id));
  }

  openQuickBookingModal(): void {
    this.bookingError.set(null);
    this.isQuickBookingModalOpen.set(true);
  }

  closeQuickBookingModal(): void {
    this.isQuickBookingModalOpen.set(false);
  }

  quickBook(dog: Dog): void {
    if (this.enrolledDogIds().has(dog.id)) return;
    this.quickBookingDogId.set(dog.id);
    this.bookingError.set(null);
    this.trainingService.quickBook$(this.trainingId(), dog.id).subscribe({
      next: (booking) => {
        this.bookings.update((bookings) => [...bookings, booking]);
        this.training.update((training) =>
          training ? { ...training, bookedCount: training.bookedCount + 1 } : training,
        );
        this.quickBookingDogId.set(null);
        this.closeQuickBookingModal();
      },
      error: (error) => {
        this.bookingError.set(error.error?.message ?? 'Nie udało się zapisać psa.');
        this.quickBookingDogId.set(null);
      },
    });
  }

  togglePaw(): void {
    if (!this.auth.currentUser()) return;
    this.trainingService.togglePaw$(this.trainingId()).subscribe({
      next: (training) =>
        this.training.update((current) =>
          current
            ? {
                ...current,
                pawCount: training.pawCount,
                pawedByCurrentUser: training.pawedByCurrentUser,
              }
            : current,
        ),
    });
  }

  openEditModal(): void {
    this.imageUploadError.set(null);
    this.isEditModalOpen.set(true);
  }

  closeEditModal(): void {
    this.isEditModalOpen.set(false);
  }

  updateTraining(submission: TrainingFormSubmission): void {
    this.trainingService.updateTraining$(this.trainingId(), submission.training).subscribe({
      next: (updated) => {
        if (!submission.image) {
          this.training.set(updated);
          this.closeEditModal();
          return;
        }
        this.trainingService.uploadTrainingImage$(updated.id, submission.image).subscribe({
          next: (withImage) => {
            this.training.set(withImage);
            this.closeEditModal();
          },
          error: () => {
            this.training.set(updated);
            this.closeEditModal();
            this.imageUploadError.set('Training was updated, but the image could not be uploaded.');
          },
        });
      },
      error: (error) => {
        this.imageUploadError.set(error.error?.message ?? 'Could not update training.');
      },
    });
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

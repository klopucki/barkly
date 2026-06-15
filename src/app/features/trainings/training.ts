import { Injectable, signal } from '@angular/core';
import { Training } from './training.model';
import { Booking } from '../bookings/components/booking-form/booking.model';

@Injectable({
  providedIn: 'root',
})
export class TrainingService {
  trainings = signal<Training[]>([
    {
      id: 1,
      schoolId: 1,
      title: 'Basic obedience',
      trainerName: 'Anna Kowalska',
      level: 'basic',
      startAt: '2026-07-10T17:00:00',
      capacity: 8,
      bookedCount: 3,
    },
    {
      id: 2,
      schoolId: 1,
      title: 'Puppy socialization',
      trainerName: 'Marek Nowak',
      level: 'puppy',
      startAt: '2026-07-12T10:00:00',
      capacity: null,
      bookedCount: 11,
    },
  ]);

  getTrainingById(id: number): Training | undefined {
    return this.trainings().find((t) => t.id === id);
  }

  bookTraining(trainingId: number): void {
    this.trainings.update((trainings) =>
      trainings.map((training) =>
        training.id === trainingId
          ? {
              ...training,
              bookedCount: training.bookedCount + 1,
            }
          : training,
      ),
    );
  }

  bookings = signal<Booking[]>([]);

  createBooking(
    trainingId: number,
    booking: Omit<Booking, 'id' | 'trainingId' | 'createdAt'>,
  ): void {
    const newBooking: Booking = {
      id: Date.now(),
      trainingId,
      ...booking,
      createdAt: new Date().toISOString(),
    };

    this.bookings.update((bookings) => [...bookings, newBooking]);
    this.bookTraining(trainingId);
  }

  getBookingsForTraining(trainingId: number): Booking[] {
    return this.bookings().filter((booking) => booking.trainingId === trainingId);
  }
}

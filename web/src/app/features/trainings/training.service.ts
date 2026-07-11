import { inject, Injectable } from '@angular/core';
import { Training, TrainingCreatePayload, TrainingDictionaries } from './training.model';
import { Booking } from '../bookings/components/booking-form/booking.model';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class TrainingService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/trainings';
  private readonly bookingApiUrl = '/api/booking';

  getTrainings$(): Observable<Training[]> {
    return this.http.get<Training[]>(this.apiUrl);
  }

  getTrainingById$(id: number): Observable<Training> {
    return this.http.get<Training>(`${this.apiUrl}/${id}`);
  }

  addTraining$(training: TrainingCreatePayload): Observable<Training> {
    return this.http.post<Training>(this.apiUrl, training);
  }

  updateTraining$(id: number, training: TrainingCreatePayload): Observable<Training> {
    return this.http.put<Training>(`${this.apiUrl}/${id}`, training);
  }

  getTrainingDictionaries$(): Observable<TrainingDictionaries> {
    return this.http.get<TrainingDictionaries>('/api/training-dictionaries');
  }

  uploadTrainingImage$(trainingId: number, image: File): Observable<Training> {
    const formData = new FormData();
    formData.append('image', image);
    return this.http.put<Training>(`${this.apiUrl}/${trainingId}/image`, formData);
  }

  getBookingsForTraining$(trainingId: number): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.apiUrl}/${trainingId}/bookings`);
  }

  createBooking$(
    trainingId: number,
    booking: Omit<Booking, 'id' | 'trainingId' | 'createdAt'>,
  ): Observable<Booking> {
    return this.http.post<Booking>(`${this.apiUrl}/${trainingId}/bookings`, booking);
  }

  deleteTraining(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  deleteBooking$(id: number) {
    return this.http.delete<void>(`${this.bookingApiUrl}/${id}`);
  }
}

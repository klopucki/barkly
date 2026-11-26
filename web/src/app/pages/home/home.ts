import { DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, effect, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from '../../features/auth/auth.service';
import { Dog, dogImageUrl } from '../../features/dogs/dog.model';
import { DogService } from '../../features/dogs/dog.service';
import { SchoolNews } from '../../features/schools/school.model';
import { SchoolService } from '../../features/schools/school.service';
import { GuestHome } from './guest-home/guest-home';

interface HomeSummary {
  managesSchools: boolean;
  pendingInvitations: number;
  personalBookings: {
    bookingId: number;
    trainingId: number;
    trainingTitle: string;
    dogName: string;
    startAt: string;
  }[];
  schoolTrainings: {
    trainingId: number;
    title: string;
    startAt: string;
    capacity: number | null;
    bookedCount: number;
    schoolId: number;
    schoolName: string;
  }[];
  recentRegistrations: {
    trainingId: number;
    trainingTitle: string;
    schoolName: string;
    ownerName: string;
    dogName: string;
    createdAt: string;
  }[];
}

@Component({
  selector: 'app-home',
  imports: [RouterLink, DatePipe, GuestHome],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {
  protected readonly auth = inject(AuthService);
  private readonly http = inject(HttpClient);
  private readonly dogsApi = inject(DogService);
  private readonly schoolsApi = inject(SchoolService);
  readonly summary = signal<HomeSummary | null>(null);
  readonly dogs = signal<Dog[]>([]);
  readonly articles = signal<SchoolNews[]>([]);
  readonly loading = signal(false);
  readonly dogsLoading = signal(false);
  readonly articlesLoading = signal(false);
  readonly summaryError = signal(false);
  readonly dogsError = signal(false);
  readonly articlesError = signal(false);
  readonly dogImageUrl = dogImageUrl;
  readonly retryKey = signal(0);
  constructor() {
    effect((onCleanup) => {
      const user = this.auth.currentUser();
      this.retryKey();
      this.summary.set(null);
      this.dogs.set([]);
      this.articles.set([]);
      this.summaryError.set(false);
      this.dogsError.set(false);
      this.articlesError.set(false);
      if (!user) {
        this.loading.set(false);
        return;
      }
      this.loading.set(true);
      this.dogsLoading.set(true);
      this.articlesLoading.set(true);
      const requests = new Subscription();
      requests.add(
        this.http.get<HomeSummary>('/api/my/home').subscribe({
          next: (value) => {
            this.summary.set(value);
            this.loading.set(false);
          },
          error: () => {
            this.summaryError.set(true);
            this.loading.set(false);
          },
        }),
      );
      requests.add(
        this.dogsApi.mine$().subscribe({
          next: (value) => {
            this.dogs.set(value);
            this.dogsLoading.set(false);
          },
          error: () => {
            this.dogsError.set(true);
            this.dogsLoading.set(false);
          },
        }),
      );
      requests.add(
        this.schoolsApi.searchNews$('', 0, 3).subscribe({
          next: (value) => {
            this.articles.set(value.items);
            this.articlesLoading.set(false);
          },
          error: () => {
            this.articlesError.set(true);
            this.articlesLoading.set(false);
          },
        }),
      );
      onCleanup(() => requests.unsubscribe());
    });
  }
  retry(): void {
    this.retryKey.update((value) => value + 1);
  }
  firstName(name: string): string {
    return name.trim().split(/\s+/)[0] || name;
  }
}

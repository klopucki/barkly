import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TrainingService } from '../../features/trainings/training.service';
import { MyTrainingBooking } from '../../features/bookings/components/booking-form/booking.model';

type CalendarView = 'upcoming' | 'past' | 'month';

interface CalendarDay {
  date: Date;
  currentMonth: boolean;
  entries: MyTrainingBooking[];
}

@Component({
  selector: 'app-training-calendar',
  imports: [DatePipe, RouterLink],
  templateUrl: './training-calendar.html',
  styleUrl: './training-calendar.css',
})
export class TrainingCalendar implements OnInit {
  private readonly trainings = inject(TrainingService);
  readonly items = signal<MyTrainingBooking[]>([]);
  readonly loading = signal(true);
  readonly view = signal<CalendarView>('upcoming');
  readonly month = signal(startOfMonth(new Date()));
  readonly now = new Date();
  readonly upcoming = computed(() =>
    this.items().filter((item) => new Date(item.startAt) >= this.now),
  );
  readonly past = computed(() =>
    this.items()
      .filter((item) => new Date(item.startAt) < this.now)
      .reverse(),
  );
  readonly today = computed(() =>
    this.upcoming().filter((item) => isSameDay(new Date(item.startAt), this.now)),
  );
  readonly days = computed(() => calendarDays(this.month(), this.items()));

  ngOnInit(): void {
    this.trainings.getMyBookings$().subscribe({
      next: (items) => {
        this.items.set(items);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  setView(view: CalendarView): void {
    this.view.set(view);
  }

  previousMonth(): void {
    this.month.update((month) => new Date(month.getFullYear(), month.getMonth() - 1, 1));
  }

  nextMonth(): void {
    this.month.update((month) => new Date(month.getFullYear(), month.getMonth() + 1, 1));
  }

  protected isToday(date: Date): boolean {
    return isSameDay(date, this.now);
  }
}

function startOfMonth(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth(), 1);
}

function isSameDay(left: Date, right: Date): boolean {
  return (
    left.getFullYear() === right.getFullYear() &&
    left.getMonth() === right.getMonth() &&
    left.getDate() === right.getDate()
  );
}

function calendarDays(month: Date, entries: MyTrainingBooking[]): CalendarDay[] {
  const first = new Date(month.getFullYear(), month.getMonth(), 1);
  const firstVisible = new Date(first);
  firstVisible.setDate(first.getDate() - ((first.getDay() + 6) % 7));
  return Array.from({ length: 42 }, (_, index) => {
    const date = new Date(firstVisible);
    date.setDate(firstVisible.getDate() + index);
    return {
      date,
      currentMonth: date.getMonth() === month.getMonth(),
      entries: entries.filter((entry) => isSameDay(new Date(entry.startAt), date)),
    };
  });
}

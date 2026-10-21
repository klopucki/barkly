import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import {
  NotificationItem,
  NotificationService,
} from '../../features/notifications/notification.service';

@Component({
  selector: 'app-notifications',
  imports: [DatePipe, RouterLink],
  templateUrl: './notifications.html',
})
export class Notifications implements OnInit {
  readonly api = inject(NotificationService);
  private readonly destroyRef = inject(DestroyRef);
  readonly items = signal<NotificationItem[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly reminders = signal<boolean | null>(null);
  readonly saving = signal(false);
  readonly hasMore = signal(false);
  private page = 0;

  ngOnInit(): void {
    this.load(true);
    this.api
      .preferences$()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (p) => this.reminders.set(p.remindersEnabled),
        error: () => this.error.set('Nie udało się pobrać ustawień. Odśwież stronę.'),
      });
  }

  load(reset = false): void {
    if (this.loading()) return;
    const page = reset ? 0 : this.page + 1;
    this.loading.set(true);
    this.error.set('');
    this.api
      .list$(page)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (items) => {
          this.items.update((previous) => (reset ? items : [...previous, ...items]));
          this.page = page;
          this.hasMore.set(items.length === 30);
          this.loading.set(false);
          this.api.refresh();
        },
        error: () => {
          this.loading.set(false);
          this.error.set('Nie udało się pobrać powiadomień. Spróbuj ponownie.');
        },
      });
  }

  markRead(item: NotificationItem): void {
    if (item.read) return;
    this.api
      .read$(item.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () =>
          this.items.update((items) =>
            items.map((n) => (n.id === item.id ? { ...n, read: true } : n)),
          ),
        error: () => this.error.set('Nie udało się oznaczyć powiadomienia jako przeczytane.'),
      });
  }

  markAllRead(): void {
    this.api
      .readAll$()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.items.update((items) => items.map((n) => ({ ...n, read: true }))),
        error: () => this.error.set('Nie udało się oznaczyć powiadomień jako przeczytane.'),
      });
  }

  toggleReminders(): void {
    const current = this.reminders();
    if (current === null || this.saving()) return;
    this.saving.set(true);
    this.api
      .setPreferences$(!current)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (p) => {
          this.reminders.set(p.remindersEnabled);
          this.saving.set(false);
        },
        error: () => {
          this.saving.set(false);
          this.error.set('Nie udało się zapisać ustawień przypomnień.');
        },
      });
  }
}

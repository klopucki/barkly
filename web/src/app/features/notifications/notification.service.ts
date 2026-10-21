import { effect, inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, of, Subject, switchMap, tap, timer } from 'rxjs';
import { AuthService } from '../auth/auth.service';

export interface NotificationItem {
  id: number;
  type: string;
  message: string;
  link: string;
  createdAt: string;
  read: boolean;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly base = '/api/my/notifications';
  private readonly refreshRequests = new Subject<void>();
  readonly unreadCount = signal(0);

  constructor() {
    effect((onCleanup) => {
      const user = this.auth.currentUser();
      this.unreadCount.set(0);
      if (!user) return;
      const subscription = this.refreshRequests
        .pipe(
          switchMap(() => timer(0, 15000)),
          switchMap(() =>
            this.http.get<number>(`${this.base}/unread-count`).pipe(catchError(() => of(null))),
          ),
        )
        .subscribe((count) => {
          if (count !== null) this.unreadCount.set(count);
        });
      this.refresh();
      onCleanup(() => subscription.unsubscribe());
    });
  }

  refresh(): void {
    this.refreshRequests.next();
  }
  list$(page = 0) {
    return this.http.get<NotificationItem[]>(this.base, { params: { page } });
  }
  read$(id: number) {
    return this.http.put<void>(`${this.base}/${id}/read`, {}).pipe(tap(() => this.refresh()));
  }
  readAll$() {
    return this.http.put<void>(`${this.base}/read-all`, {}).pipe(tap(() => this.refresh()));
  }
  preferences$() {
    return this.http.get<{ remindersEnabled: boolean }>(`${this.base}/preferences`);
  }
  setPreferences$(remindersEnabled: boolean) {
    return this.http.put<{ remindersEnabled: boolean }>(`${this.base}/preferences`, {
      remindersEnabled,
    });
  }
}

import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NotificationService } from './notification.service';

@Component({
  selector: 'app-notification-bell',
  imports: [RouterLink],
  template: `
    <a
      routerLink="/me/notifications"
      class="relative flex min-h-11 min-w-11 items-center justify-center rounded-lg hover:bg-stone-200"
      [attr.aria-label]="'Powiadomienia, nieprzeczytane: ' + notifications.unreadCount()"
      title="Powiadomienia"
    >
      <svg
        aria-hidden="true"
        width="22"
        height="22"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="1.8"
      >
        <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4" />
      </svg>
      @if (notifications.unreadCount() > 0) {
        <span
          class="absolute -right-1 top-0 rounded-full bg-emerald-800 px-1.5 text-xs font-bold text-white"
          aria-hidden="true"
        >
          {{ notifications.unreadCount() > 99 ? '99+' : notifications.unreadCount() }}
        </span>
      }
    </a>
  `,
})
export class NotificationBell {
  readonly notifications = inject(NotificationService);
}

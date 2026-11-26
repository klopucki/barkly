import { Component, inject, input, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AccessInvitation, AccessService } from './access.service';

@Component({
  selector: 'app-manage-access',
  imports: [FormsModule],
  template: `
    <p class="text-sm text-stone-600">
      Zaproś osobę, która ma już konto w Barkly. Dostęp zostanie przyznany po przyjęciu zaproszenia.
    </p>
    <form class="mt-4 space-y-3" (ngSubmit)="invite()">
      <label class="block text-sm font-semibold"
        >Adres e-mail
        <input
          class="mt-1 block w-full rounded-lg border p-2 font-normal"
          type="email"
          name="email"
          [(ngModel)]="email"
          required
        />
      </label>
      <button
        class="barkly-button disabled:opacity-50"
        type="submit"
        [disabled]="busy() || !email.trim()"
      >
        Wyślij zaproszenie
      </button>
    </form>
    @if (error()) {
      <p role="alert" class="mt-3 text-red-700">{{ error() }}</p>
    }
    @if (message()) {
      <p role="status" class="mt-3 text-emerald-700">{{ message() }}</p>
    }
    <h3 class="mt-6 font-semibold">
      {{ type() === 'DOG' ? 'Współopiekunowie' : 'Współwłaściciele' }}
    </h3>
    @if (loading()) {
      <p class="mt-2 text-sm text-stone-600">Ładowanie…</p>
    }
    @for (member of members(); track member.id) {
      <div class="mt-2 flex flex-wrap items-center justify-between gap-2 rounded-lg border p-3">
        <span
          >{{ member.inviteeName }} —
          {{ member.status === 'PENDING' ? 'oczekuje na zaproszenie' : 'ma dostęp' }}</span
        >
        <button
          type="button"
          class="rounded border px-3 py-2 text-sm text-red-700"
          [disabled]="busy()"
          (click)="revoke(member)"
        >
          Cofnij dostęp
        </button>
      </div>
    } @empty {
      @if (!loading() && !error()) {
        <p class="mt-2 text-sm text-stone-600">Brak zaproszonych osób.</p>
      }
    }
  `,
})
export class ManageAccess implements OnInit {
  readonly type = input.required<'DOG' | 'SCHOOL'>();
  readonly resourceId = input.required<number>();
  private readonly access = inject(AccessService);
  readonly members = signal<AccessInvitation[]>([]);
  readonly loading = signal(true);
  readonly busy = signal(false);
  readonly error = signal('');
  readonly message = signal('');
  email = '';
  ngOnInit(): void {
    this.refresh();
  }
  refresh(): void {
    this.loading.set(true);
    this.access.members(this.type(), this.resourceId()).subscribe({
      next: (members) => {
        this.members.set(members);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.error.set('Nie udało się pobrać listy osób.');
      },
    });
  }
  invite(): void {
    const email = this.email.trim();
    if (!email || this.busy()) return;
    this.busy.set(true);
    this.error.set('');
    this.message.set('');
    this.access.invite(this.type(), this.resourceId(), email).subscribe({
      next: () => {
        this.busy.set(false);
        this.email = '';
        this.message.set('Zaproszenie zostało wysłane.');
        this.refresh();
      },
      error: (error) => {
        this.busy.set(false);
        this.error.set(
          error.status === 409
            ? 'Ta osoba ma już dostęp lub oczekuje na zaproszenie.'
            : 'Nie udało się wysłać zaproszenia. Sprawdź adres i spróbuj ponownie.',
        );
      },
    });
  }
  revoke(member: AccessInvitation): void {
    if (this.busy()) return;
    this.busy.set(true);
    this.error.set('');
    this.message.set('');
    this.access.revoke(member).subscribe({
      next: () => {
        this.busy.set(false);
        this.message.set('Dostęp został cofnięty.');
        this.refresh();
      },
      error: () => {
        this.busy.set(false);
        this.error.set('Nie udało się cofnąć dostępu.');
      },
    });
  }
}

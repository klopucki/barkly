import { Component, inject, input, output, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Modal } from '../../../shared/components/modal/modal';

@Component({
  selector: 'app-message-form',
  imports: [FormsModule, Modal],
  template: `
    <form class="space-y-3 rounded-xl border border-stone-200 p-4" (ngSubmit)="openConfirmation()">
      <label class="block font-medium" [for]="'message-' + audience() + '-' + id()">{{
        label()
      }}</label>
      <textarea
        [id]="'message-' + audience() + '-' + id()"
        class="w-full rounded-lg border p-3"
        name="message"
        [(ngModel)]="message"
        maxlength="1000"
        rows="3"
        required
      ></textarea>
      <button type="submit" class="barkly-button" [disabled]="sending() || !message.trim()">
        {{ sending() ? 'Wysyłanie…' : 'Wyślij powiadomienie' }}
      </button>
      @if (status()) {
        <p role="status">{{ status() }}</p>
      }
    </form>
    @if (confirming()) {
      <app-modal title="Wysłać powiadomienie?" (closed)="confirming.set(false)">
        <p class="text-stone-700">
          {{
            audience() === 'schools'
              ? 'Wiadomość trafi do subskrybentów tej szkoły.'
              : 'Wiadomość trafi do osób zapisanych na ten trening i będzie widoczna w historii treningu dla jego uczestników.'
          }}
          Wysłanej wiadomości nie można cofnąć.
        </p>
        <p class="mt-4 whitespace-pre-wrap rounded-lg bg-stone-50 p-3">{{ message.trim() }}</p>
        <div class="mt-5 flex gap-3">
          <button type="button" class="barkly-button" [disabled]="sending()" (click)="send()">
            {{ sending() ? 'Wysyłanie…' : 'Tak, wyślij' }}</button
          ><button
            type="button"
            class="rounded-full border px-4 py-2"
            [disabled]="sending()"
            (click)="confirming.set(false)"
          >
            Anuluj
          </button>
        </div>
      </app-modal>
    }
  `,
})
export class MessageForm {
  audience = input.required<'schools' | 'trainings'>();
  id = input.required<number>();
  label = input.required<string>();
  sent = output<void>();
  message = '';
  sending = signal(false);
  confirming = signal(false);
  status = signal('');
  private readonly http = inject(HttpClient);

  openConfirmation(): void {
    if (!this.message.trim() || this.message.trim().length > 1000 || this.sending()) return;
    this.confirming.set(true);
  }

  send(): void {
    const message = this.message.trim();
    if (!message || message.length > 1000 || this.sending()) return;
    this.sending.set(true);
    this.status.set('');
    this.http.post<void>(`/api/${this.audience()}/${this.id()}/messages`, { message }).subscribe({
      next: () => {
        this.message = '';
        this.confirming.set(false);
        this.sending.set(false);
        this.status.set('Powiadomienie zostało wysłane.');
        this.sent.emit();
      },
      error: () => {
        this.sending.set(false);
        this.confirming.set(false);
        this.status.set('Nie udało się wysłać powiadomienia. Spróbuj ponownie.');
      },
    });
  }
}

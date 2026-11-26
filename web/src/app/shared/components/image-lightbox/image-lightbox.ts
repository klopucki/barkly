import { Component, HostListener, input, output } from '@angular/core';

@Component({
  selector: 'app-image-lightbox',
  template: `
    @if (src(); as image) {
      <div
        class="fixed inset-0 z-[100] flex items-center justify-center bg-stone-950/90 p-4"
        role="dialog"
        aria-modal="true"
        aria-label="Powiększone zdjęcie"
        (click)="closed.emit()"
      >
        <img
          class="max-h-[85vh] max-w-full rounded-xl object-contain"
          [src]="image"
          [alt]="alt()"
          (click)="$event.stopPropagation()"
        />
        <button
          type="button"
          class="absolute right-4 top-4 rounded-full bg-white px-4 py-2 text-xl text-stone-900"
          aria-label="Zamknij zdjęcie"
          (click)="closed.emit()"
        >
          ×
        </button>
      </div>
    }
  `,
})
export class ImageLightbox {
  readonly src = input<string | null>(null);
  readonly alt = input('');
  readonly closed = output<void>();
  @HostListener('document:keydown.escape') onEscape(): void {
    if (this.src()) this.closed.emit();
  }
}

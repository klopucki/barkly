import { Component, computed, input, signal } from '@angular/core';

export interface GalleryImage {
  src: string;
  alt: string;
}

@Component({
  selector: 'app-image-gallery',
  templateUrl: './image-gallery.html',
})
export class ImageGallery {
  images = input.required<GalleryImage[]>();
  readonly currentIndex = signal(0);
  readonly isOpen = signal(false);
  readonly selectedIndex = computed(() =>
    Math.min(this.currentIndex(), Math.max(0, this.images().length - 1)),
  );
  readonly current = computed(() => this.images()[this.selectedIndex()] ?? null);

  select(index: number): void {
    this.currentIndex.set(index);
  }

  open(index = this.selectedIndex()): void {
    this.select(index);
    this.isOpen.set(true);
  }

  close(): void {
    this.isOpen.set(false);
  }

  previous(): void {
    if (this.images().length)
      this.currentIndex.set(
        (this.selectedIndex() - 1 + this.images().length) % this.images().length,
      );
  }

  next(): void {
    if (this.images().length)
      this.currentIndex.set((this.selectedIndex() + 1) % this.images().length);
  }
}

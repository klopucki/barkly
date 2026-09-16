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
  readonly current = computed(() => this.images()[this.currentIndex()] ?? null);

  select(index: number): void {
    this.currentIndex.set(index);
  }

  open(index = this.currentIndex()): void {
    this.select(index);
    this.isOpen.set(true);
  }

  close(): void {
    this.isOpen.set(false);
  }

  previous(): void {
    this.currentIndex.update((index) => (index - 1 + this.images().length) % this.images().length);
  }

  next(): void {
    this.currentIndex.update((index) => (index + 1) % this.images().length);
  }
}

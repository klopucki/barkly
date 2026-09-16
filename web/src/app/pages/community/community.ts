import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../features/auth/auth.service';
import { Dog, DogPost, dogPostImageUrl } from '../../features/dogs/dog.model';
import { DogService } from '../../features/dogs/dog.service';
import { GalleryImage, ImageGallery } from '../../shared/components/image-gallery/image-gallery';

@Component({
  selector: 'app-community',
  imports: [DatePipe, FormsModule, RouterLink, ImageGallery],
  templateUrl: './community.html',
})
export class Community implements OnInit {
  private readonly dogsApi = inject(DogService);
  protected readonly auth = inject(AuthService);
  readonly posts = signal<DogPost[]>([]);
  readonly dogs = signal<Dog[]>([]);
  selectedDogId: number | null = null;
  content = '';
  image: File | null = null;
  error = '';
  saving = false;

  ngOnInit(): void {
    this.loadPosts();
    if (this.auth.currentUser()) {
      this.dogsApi.mine$().subscribe({ next: (dogs) => this.dogs.set(dogs) });
    }
  }

  loadPosts(): void {
    this.dogsApi.posts$().subscribe({
      next: (posts) => this.posts.set(posts),
      error: () => (this.error = 'Nie udało się pobrać aktualności.'),
    });
  }

  selectImage(event: Event): void {
    this.image = (event.target as HTMLInputElement).files?.[0] ?? null;
  }

  galleryImages(post: DogPost): GalleryImage[] {
    return post.hasImage
      ? [{ src: dogPostImageUrl(post.id), alt: `Zdjęcie psa ${post.dogName}` }]
      : [];
  }

  publish(): void {
    if (!this.selectedDogId || !this.content.trim()) {
      this.error = 'Wybierz psa i napisz krótką aktualność.';
      return;
    }
    this.saving = true;
    this.error = '';
    this.dogsApi.createPost$(this.selectedDogId, this.content.trim(), this.image).subscribe({
      next: (post) => {
        this.posts.update((posts) => [post, ...posts]);
        this.content = '';
        this.image = null;
        this.saving = false;
      },
      error: (e) => {
        this.error = e.error?.message ?? 'Nie udało się opublikować aktualności.';
        this.saving = false;
      },
    });
  }
}

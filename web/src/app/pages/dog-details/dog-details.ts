import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Dog, dogImageUrl } from '../../features/dogs/dog.model';
import { DogService } from '../../features/dogs/dog.service';
import { GalleryImage, ImageGallery } from '../../shared/components/image-gallery/image-gallery';

@Component({
  selector: 'app-dog-details',
  imports: [RouterLink, ImageGallery],
  templateUrl: './dog-details.html',
})
export class DogDetails implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(DogService);

  readonly dog = signal<Dog | null>(null);
  readonly imageUrl = dogImageUrl;
  readonly error = signal('');
  readonly galleryImages = computed<GalleryImage[]>(
    () =>
      this.dog()?.images.map((image) => ({ src: dogImageUrl(image.id), alt: this.dog()!.name })) ??
      [],
  );

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.api.one$(id).subscribe({
      next: (dog) => this.dog.set(dog),
      error: () => this.error.set('Ten profil nie jest dostępny.'),
    });
  }
}

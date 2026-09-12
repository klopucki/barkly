import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Dog, dogImageUrl, DogPayload, DogVisibility } from '../../features/dogs/dog.model';
import { DogService } from '../../features/dogs/dog.service';
import { Modal } from '../../shared/components/modal/modal';

const empty = (): DogPayload => ({
  name: '',
  breed: null,
  birthDate: null,
  sex: null,
  description: '',
  visibility: 'MEMBERS',
});

@Component({
  selector: 'app-my-dogs',
  imports: [FormsModule, Modal],
  templateUrl: './my-dogs.html',
})
export class MyDogs implements OnInit {
  private readonly dogsApi = inject(DogService);
  readonly dogs = signal<Dog[]>([]);
  readonly editing = signal<Dog | null>(null);
  readonly isFormOpen = signal(false);
  readonly form = signal<DogPayload>(empty());
  readonly error = signal('');
  readonly imageUrl = dogImageUrl;
  ngOnInit(): void {
    this.refresh();
  }
  refresh(): void {
    this.dogsApi.mine$().subscribe({
      next: (dogs) => this.dogs.set(dogs),
      error: () => this.error.set('Nie udało się pobrać listy psów. Zaloguj się ponownie.'),
    });
  }
  add(): void {
    this.editing.set(null);
    this.form.set(empty());
    this.error.set('');
    this.isFormOpen.set(true);
  }
  edit(dog: Dog): void {
    this.editing.set(dog);
    this.form.set({
      name: dog.name,
      breed: dog.breed,
      birthDate: dog.birthDate,
      sex: dog.sex,
      description: dog.description,
      visibility: dog.visibility,
    });
    this.error.set('');
    this.isFormOpen.set(true);
  }
  cancel(): void {
    this.editing.set(null);
    this.form.set(empty());
    this.isFormOpen.set(false);
  }
  save(): void {
    const value = this.form();
    if (!value.name.trim()) {
      this.error.set('Podaj imię psa.');
      return;
    }
    const request = {
      ...value,
      name: value.name.trim(),
      breed: value.breed?.trim() || null,
      sex: value.sex?.trim() || null,
    };
    const current = this.editing();
    const action = current
      ? this.dogsApi.update$(current.id, request)
      : this.dogsApi.create$(request);
    action.subscribe({
      next: () => {
        this.refresh();
        this.cancel();
      },
      error: (e) => this.error.set(e.error?.message ?? 'Nie udało się zapisać profilu.'),
    });
  }
  delete(dog: Dog): void {
    if (!confirm(`Usunąć profil ${dog.name} wraz ze zdjęciami?`)) return;
    this.dogsApi.delete$(dog.id).subscribe({
      next: () => {
        this.cancel();
        this.refresh();
      },
      error: () => this.error.set('Nie udało się usunąć profilu.'),
    });
  }
  upload(event: Event, dog: Dog): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.dogsApi.uploadImage$(dog.id, file, dog.visibility).subscribe({
      next: () => this.refresh(),
      error: (e) => this.error.set(e.error?.message ?? 'Nie udało się przesłać zdjęcia.'),
    });
  }
  deleteImage(id: number): void {
    this.dogsApi.deleteImage$(id).subscribe({
      next: () => this.refresh(),
      error: () => this.error.set('Nie udało się usunąć zdjęcia.'),
    });
  }
  set<K extends keyof DogPayload>(key: K, value: DogPayload[K]): void {
    this.form.update((current) => ({ ...current, [key]: value }));
  }
  visibilityLabel(visibility: DogVisibility): string {
    return { PRIVATE: 'Prywatny', MEMBERS: 'Dla użytkowników Barkly', PUBLIC: 'Publiczny' }[
      visibility
    ];
  }
}

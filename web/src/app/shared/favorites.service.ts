import { effect, Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../features/auth/auth.service';

export type FavoriteKind = 'school' | 'training' | 'article';

@Injectable({ providedIn: 'root' })
export class FavoritesService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly keys = signal<string[]>([]);

  constructor() {
    effect(() => {
      if (this.auth.currentUser()) this.load();
      else this.keys.set([]);
    });
  }

  has(kind: FavoriteKind, id: number): boolean {
    return this.keys().includes(this.key(kind, id));
  }

  toggle(kind: FavoriteKind, id: number): void {
    if (!this.auth.currentUser()) return;
    const key = this.key(kind, id);
    const wasFavorite = this.keys().includes(key);
    this.keys.update((keys) =>
      wasFavorite ? keys.filter((item) => item !== key) : [...keys, key],
    );
    const request = wasFavorite
      ? this.http.delete(`/api/favorites/${kind}/${id}`)
      : this.http.put(`/api/favorites/${kind}/${id}`, {});
    request.subscribe({
      error: () =>
        this.keys.update((keys) =>
          wasFavorite ? [...keys, key] : keys.filter((item) => item !== key),
        ),
    });
  }

  private key(kind: FavoriteKind, id: number): string {
    return `${kind}:${id}`;
  }

  private load(): void {
    this.http.get<{ type: FavoriteKind; id: number }[]>('/api/favorites').subscribe({
      next: (items) => this.keys.set(items.map((item) => this.key(item.type, item.id))),
      error: () => this.keys.set([]),
    });
  }
}

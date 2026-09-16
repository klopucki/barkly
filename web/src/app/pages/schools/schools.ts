import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SchoolService } from '../../features/schools/school.service';
import { School } from '../../features/schools/school.model';
import { AuthService } from '../../features/auth/auth.service';
import { FavoritesService } from '../../shared/favorites.service';

@Component({
  selector: 'app-schools',
  imports: [RouterLink],
  templateUrl: './schools.html',
  styleUrl: './schools.css',
})
export class Schools implements OnInit {
  private readonly schools = inject(SchoolService);
  protected readonly auth = inject(AuthService);
  protected readonly favorites = inject(FavoritesService);
  readonly items = signal<School[]>([]);
  readonly query = signal('');
  readonly favoritesOnly = signal(false);
  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly loading = signal(false);
  private searchTimer?: ReturnType<typeof setTimeout>;
  readonly filteredItems = computed(() => {
    const query = this.query().trim().toLowerCase();
    return this.items().filter(
      (school) =>
        (!query ||
          [school.name, school.address, school.description, school.activities].some((value) =>
            value.toLowerCase().includes(query),
          )) &&
        (!this.favoritesOnly() || this.favorites.has('school', school.id)),
    );
  });
  ngOnInit(): void {
    this.load(0);
  }

  search(query: string): void {
    this.query.set(query);
    clearTimeout(this.searchTimer);
    this.searchTimer = setTimeout(() => this.load(0), 250);
  }

  loadMore(): void {
    this.load(this.page() + 1, true);
  }

  toggleFavorites(): void {
    if (!this.auth.currentUser()) return;
    this.favoritesOnly.set(!this.favoritesOnly());
    this.load(0);
  }

  private load(page: number, append = false): void {
    this.loading.set(true);
    this.schools.search$(this.query(), page, 20, this.favoritesOnly()).subscribe({
      next: (result) => {
        this.items.set(append ? [...this.items(), ...result.items] : result.items);
        this.page.set(result.page);
        this.totalPages.set(result.totalPages);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}

import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { SchoolService } from '../../features/schools/school.service';
import { SchoolNews } from '../../features/schools/school.model';
import { FavoritesService } from '../../shared/favorites.service';
import { AuthService } from '../../features/auth/auth.service';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-news',
  imports: [DatePipe, RouterLink],
  templateUrl: './news.html',
  styleUrl: './news.css',
})
export class News implements OnInit {
  private readonly schools = inject(SchoolService);
  protected readonly auth = inject(AuthService);
  protected readonly favorites = inject(FavoritesService);
  readonly items = signal<SchoolNews[]>([]);
  readonly query = signal('');
  readonly favoritesOnly = signal(false);
  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly loading = signal(false);
  private searchTimer?: ReturnType<typeof setTimeout>;
  readonly filteredItems = computed(() => {
    const query = this.query().trim().toLowerCase();
    return this.items().filter(
      (item) =>
        (!query ||
          [item.title, item.schoolName, item.content].some((value) =>
            value.toLowerCase().includes(query),
          )) &&
        (!this.favoritesOnly() || this.favorites.has('article', item.id)),
    );
  });
  ngOnInit() {
    this.load(0);
  }

  search(query: string): void {
    this.query.set(query);
    clearTimeout(this.searchTimer);
    this.searchTimer = setTimeout(() => this.load(0), 250);
  }

  excerpt(content: string): string {
    const text = content
      .replace(/<[^>]*>/g, ' ')
      .replace(/&nbsp;/g, ' ')
      .replace(/\s+/g, ' ')
      .trim();
    return text.length > 190 ? `${text.slice(0, 187).trimEnd()}…` : text;
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
    this.schools.searchNews$(this.query(), page, 20, this.favoritesOnly()).subscribe({
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

import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../features/auth/auth.service';
import { School, SchoolNews } from '../../features/schools/school.model';
import { SchoolService } from '../../features/schools/school.service';
import { Training, trainingPriceLabel } from '../../features/trainings/training.model';
import { TrainingService } from '../../features/trainings/training.service';
import { FavoritesService } from '../../shared/favorites.service';

@Component({
  selector: 'app-me',
  imports: [RouterLink, FormsModule],
  templateUrl: './me.html',
})
export class Me implements OnInit {
  protected editingName = signal(false);
  protected savingName = signal(false);
  protected nameError = signal('');
  protected displayName = '';

  protected editName(): void {
    this.displayName = this.auth.currentUser()?.displayName ?? '';
    this.nameError.set('');
    this.editingName.set(true);
  }

  protected saveName(): void {
    const name = this.displayName.trim();
    if (name.length < 2 || name.length > 100) {
      this.nameError.set('Nazwa musi mieć od 2 do 100 znaków.');
      return;
    }
    this.savingName.set(true);
    this.nameError.set('');
    this.auth.updateDisplayName$(name).subscribe({
      next: () => {
        this.savingName.set(false);
        this.editingName.set(false);
      },
      error: () => {
        this.savingName.set(false);
        this.nameError.set('Nie udało się zapisać nazwy. Spróbuj ponownie.');
      },
    });
  }

  protected readonly trainingPriceLabel = trainingPriceLabel;
  protected readonly auth = inject(AuthService);
  private readonly schoolsApi = inject(SchoolService);
  private readonly trainingsApi = inject(TrainingService);
  protected readonly favorites = inject(FavoritesService);
  private readonly schools = signal<School[]>([]);
  private readonly trainings = signal<Training[]>([]);
  private readonly articles = signal<SchoolNews[]>([]);
  readonly favoriteSchools = computed(() =>
    this.schools().filter((item) => this.favorites.has('school', item.id)),
  );
  readonly favoriteTrainings = computed(() =>
    this.trainings().filter((item) => this.favorites.has('training', item.id)),
  );
  readonly favoriteArticles = computed(() =>
    this.articles().filter((item) => this.favorites.has('article', item.id)),
  );

  ngOnInit(): void {
    this.schoolsApi
      .search$('', 0, 20, true)
      .subscribe({ next: (result) => this.schools.set(result.items) });
    this.trainingsApi
      .search$('', 0, 20, true)
      .subscribe({ next: (result) => this.trainings.set(result.items) });
    this.schoolsApi
      .searchNews$('', 0, 20, true)
      .subscribe({ next: (result) => this.articles.set(result.items) });
  }

  protected canManageSchools(): boolean {
    const role = this.auth.currentUser()?.role;
    return role === 'SCHOOL_ADMIN' || role === 'SUPER_ADMIN';
  }
}

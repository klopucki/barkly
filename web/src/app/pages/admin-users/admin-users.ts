import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { AuthService, CurrentUser, UserRole } from '../../features/auth/auth.service';
import { PageResult } from '../../shared/page-result';
import { Modal } from '../../shared/components/modal/modal';

interface AdminUser extends CurrentUser {
  active: boolean;
}

@Component({
  selector: 'app-admin-users',
  imports: [FormsModule, Modal],
  templateUrl: './admin-users.html',
})
export class AdminUsers implements OnInit {
  readonly auth = inject(AuthService);
  private readonly http = inject(HttpClient);
  private readonly destroyRef = inject(DestroyRef);
  readonly result = signal<PageResult<AdminUser> | null>(null);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly success = signal('');
  readonly selected = signal<AdminUser | null>(null);
  readonly roles: { value: UserRole; label: string }[] = [
    { value: 'USER', label: 'Użytkownik' },
    { value: 'SCHOOL_ADMIN', label: 'Administrator szkoły' },
    { value: 'SUPER_ADMIN', label: 'Administrator serwisu' },
  ];
  query = '';
  private appliedQuery = '';
  role: UserRole = 'USER';
  active = true;

  ngOnInit(): void {
    this.search();
  }

  search(): void {
    if (this.loading() || this.saving()) return;
    this.appliedQuery = this.query.trim();
    this.load(0);
  }

  load(page: number): void {
    if (this.loading() || this.saving()) return;
    this.loading.set(true);
    this.error.set('');
    this.http
      .get<PageResult<AdminUser>>('/api/admin/users', {
        params: { query: this.appliedQuery, page, size: 20 },
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (result) => {
          this.result.set(result);
          this.loading.set(false);
        },
        error: (error: HttpErrorResponse) => {
          this.loading.set(false);
          this.error.set(
            this.errorMessage(error, 'Nie udało się pobrać użytkowników. Spróbuj ponownie.'),
          );
        },
      });
  }

  roleLabel(role: UserRole): string {
    return this.roles.find((item) => item.value === role)?.label ?? role;
  }

  edit(user: AdminUser): void {
    if (user.id === this.auth.currentUser()?.id) return;
    this.role = user.role;
    this.active = user.active;
    this.error.set('');
    this.success.set('');
    this.selected.set(user);
  }

  close(): void {
    if (!this.saving()) this.selected.set(null);
  }

  save(): void {
    const user = this.selected();
    if (!user || this.saving()) return;
    this.saving.set(true);
    this.error.set('');
    this.http
      .put<AdminUser>(`/api/admin/users/${user.id}`, {
        role: this.role,
        active: this.active,
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (updated) => {
          this.result.update((result) =>
            result
              ? {
                  ...result,
                  items: result.items.map((item) => (item.id === updated.id ? updated : item)),
                }
              : null,
          );
          this.saving.set(false);
          this.selected.set(null);
          this.success.set(`Zapisano zmiany konta ${updated.displayName}.`);
        },
        error: (error: HttpErrorResponse) => {
          this.saving.set(false);
          this.error.set(
            this.errorMessage(
              error,
              error.status === 409
                ? 'Nie możesz zmienić dostępu do własnego konta.'
                : 'Nie udało się zapisać zmian. Odśwież listę i spróbuj ponownie.',
            ),
          );
        },
      });
  }

  private errorMessage(error: HttpErrorResponse, fallback: string): string {
    return error.status === 401 || error.status === 403
      ? 'Brak dostępu. Zaloguj się ponownie na konto administratora serwisu.'
      : fallback;
  }
}

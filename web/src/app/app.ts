import { NotificationBell } from './features/notifications/notification-bell';
import { Component, HostListener, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './features/auth/auth.service';
import { SchoolService } from './features/schools/school.service';
import { School } from './features/schools/school.model';
import { Modal } from './shared/components/modal/modal';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, NotificationBell, Modal],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly schoolsApi = inject(SchoolService);
  protected readonly articleSchools = signal<School[]>([]);
  protected readonly schoolsLoading = signal(false);
  protected readonly schoolsError = signal(false);
  protected readonly choosingArticleSchool = signal(false);
  protected readonly title = signal('barkly');
  protected readonly isDiscoverOpen = signal(false);
  protected readonly isAddOpen = signal(false);
  protected readonly isProfileOpen = signal(false);

  @HostListener('document:click')
  protected closeMenus(): void {
    this.isDiscoverOpen.set(false);
    this.isAddOpen.set(false);
    this.isProfileOpen.set(false);
  }

  protected toggleAddMenu(): void {
    const opening = !this.isAddOpen();
    this.isAddOpen.set(opening);
    this.isDiscoverOpen.set(false);
    this.isProfileOpen.set(false);
    if (!opening) return;
    const userId = this.auth.currentUser()?.id;
    if (!userId) return;
    this.articleSchools.set([]);
    this.schoolsError.set(false);
    this.schoolsLoading.set(true);
    this.schoolsApi.mine$().subscribe({
      next: (schools) => {
        if (this.auth.currentUser()?.id !== userId) return;
        this.articleSchools.set(schools);
        this.schoolsLoading.set(false);
      },
      error: () => {
        if (this.auth.currentUser()?.id !== userId) return;
        this.schoolsLoading.set(false);
        this.schoolsError.set(true);
      },
    });
  }

  protected writeArticle(): void {
    const schools = this.articleSchools();
    this.closeMenus();
    if (schools.length === 1) this.openArticleFor(schools[0]);
    else if (schools.length > 1) this.choosingArticleSchool.set(true);
  }

  protected openArticleFor(school: School): void {
    this.choosingArticleSchool.set(false);
    this.router.navigate(['/schools', school.slug], { queryParams: { newArticle: '1' } });
  }

  protected initials(): string {
    return this.auth.currentUser()?.displayName.trim().slice(0, 1).toUpperCase() ?? '?';
  }

  protected canManageSchools(): boolean {
    const role = this.auth.currentUser()?.role;
    return role === 'SCHOOL_ADMIN' || role === 'SUPER_ADMIN';
  }

  protected logout(): void {
    this.auth.logout();
    this.closeMenus();
    this.router.navigateByUrl('/');
  }
}

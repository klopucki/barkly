import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './features/auth/auth.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  protected readonly title = signal('barkly');
  protected readonly isDiscoverOpen = signal(false);
  protected readonly isAddOpen = signal(false);
  protected readonly isProfileOpen = signal(false);

  protected closeMenus(): void {
    this.isDiscoverOpen.set(false);
    this.isAddOpen.set(false);
    this.isProfileOpen.set(false);
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

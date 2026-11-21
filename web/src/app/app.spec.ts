import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { App } from './app';
import { AuthService, CurrentUser } from './features/auth/auth.service';
import { NotificationService } from './features/notifications/notification.service';

describe('App', () => {
  const currentUser = signal<CurrentUser | null>(null);

  beforeEach(async () => {
    currentUser.set(null);
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: { currentUser, logout: vi.fn() } },
        { provide: NotificationService, useValue: { unreadCount: signal(0) } },
      ],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render the Barkly brand', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Barkly');
  });

  it('shows the admin navigation only to super administrators', () => {
    const fixture = TestBed.createComponent(App);
    const link = () => fixture.nativeElement.querySelector('a[href="/admin/users"]');
    fixture.detectChanges();
    expect(link()).toBeNull();

    const user: CurrentUser = {
      id: 1,
      displayName: 'Test',
      email: 'test@example.com',
      role: 'USER',
    };
    currentUser.set(user);
    fixture.detectChanges();
    expect(link()).toBeNull();

    currentUser.set({ ...user, role: 'SCHOOL_ADMIN' });
    fixture.detectChanges();
    expect(link()).toBeNull();

    currentUser.set({ ...user, role: 'SUPER_ADMIN' });
    fixture.detectChanges();
    expect(link()?.textContent).toContain('Administracja');
  });
});

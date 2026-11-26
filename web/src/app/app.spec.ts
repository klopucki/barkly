import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { signal } from '@angular/core';
import { App } from './app';
import { AuthService, CurrentUser } from './features/auth/auth.service';
import { NotificationService } from './features/notifications/notification.service';
import { SchoolService } from './features/schools/school.service';
import { of } from 'rxjs';
import { School } from './features/schools/school.model';

describe('App', () => {
  const currentUser = signal<CurrentUser | null>(null);
  let schools: School[] = [];

  beforeEach(async () => {
    currentUser.set(null);
    schools = [];
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideRouter([]),
        { provide: SchoolService, useValue: { mine$: () => of(schools) } },
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

  it('closes dropdowns when clicking outside them', () => {
    currentUser.set({ id: 1, displayName: 'Test', email: 'test@example.com', role: 'USER' });
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const button = fixture.nativeElement.querySelector(
      'button[aria-label^="Menu użytkownika"]',
    ) as HTMLButtonElement;

    button.click();
    fixture.detectChanges();
    expect(button.getAttribute('aria-expanded')).toBe('true');
    expect(fixture.nativeElement.textContent).toContain('Moje szkoły');

    document.body.click();
    fixture.detectChanges();
    expect(button.getAttribute('aria-expanded')).toBe('false');
    expect(fixture.nativeElement.textContent).not.toContain('Moje szkoły');
  });

  it('opens the article editor directly for a co-manager of one school', () => {
    currentUser.set({ id: 1, displayName: 'Test', email: 'test@example.com', role: 'USER' });
    schools = [{ id: 4, name: 'Szkoła A', slug: 'szkola-a' } as School];
    const fixture = TestBed.createComponent(App);
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    fixture.detectChanges();
    const add = [...fixture.nativeElement.querySelectorAll('button')].find((button: any) =>
      button.textContent.includes('+ Dodaj'),
    ) as HTMLButtonElement;
    add.click();
    fixture.detectChanges();
    const write = [...fixture.nativeElement.querySelectorAll('button')].find((button: any) =>
      button.textContent.includes('Napisz artykuł'),
    ) as HTMLButtonElement;
    expect(write).toBeTruthy();
    write.click();
    expect(navigate).toHaveBeenCalledWith(['/schools', 'szkola-a'], {
      queryParams: { newArticle: '1' },
    });
  });

  it('asks which school to use when there are multiple schools', () => {
    currentUser.set({ id: 1, displayName: 'Test', email: 'test@example.com', role: 'USER' });
    schools = [
      { id: 1, name: 'Szkoła A', slug: 'a' } as School,
      { id: 2, name: 'Szkoła B', slug: 'b' } as School,
    ];
    const fixture = TestBed.createComponent(App);
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    fixture.detectChanges();
    (
      [...fixture.nativeElement.querySelectorAll('button')].find((button: any) =>
        button.textContent.includes('+ Dodaj'),
      ) as HTMLButtonElement
    ).click();
    fixture.detectChanges();
    (
      [...fixture.nativeElement.querySelectorAll('button')].find((button: any) =>
        button.textContent.includes('Napisz artykuł'),
      ) as HTMLButtonElement
    ).click();
    fixture.detectChanges();
    expect(navigate).not.toHaveBeenCalled();
    (
      [...fixture.nativeElement.querySelectorAll('button')].find((button: any) =>
        button.textContent.includes('Szkoła B'),
      ) as HTMLButtonElement
    ).click();
    expect(navigate).toHaveBeenCalledWith(['/schools', 'b'], { queryParams: { newArticle: '1' } });
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

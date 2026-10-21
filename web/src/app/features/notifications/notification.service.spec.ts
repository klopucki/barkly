import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { AuthService, CurrentUser } from '../auth/auth.service';
import { NotificationService } from './notification.service';

describe('NotificationService', () => {
  const user: CurrentUser = { id: 1, email: 'user@example.com', displayName: 'User', role: 'USER' };
  let currentUser: ReturnType<typeof signal<CurrentUser | null>>;
  let http: HttpTestingController;
  let service: NotificationService;

  beforeEach(() => {
    vi.useFakeTimers();
    currentUser = signal<CurrentUser | null>(null);
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: { currentUser } },
      ],
    });
    http = TestBed.inject(HttpTestingController);
    service = TestBed.inject(NotificationService);
  });
  afterEach(() => {
    http.verify();
    TestBed.resetTestingModule();
    vi.useRealTimers();
  });

  it('does not poll for anonymous visitors and clears the counter on logout', () => {
    TestBed.tick();
    vi.advanceTimersByTime(15000);
    http.expectNone('/api/my/notifications/unread-count');
    currentUser.set(user);
    TestBed.tick();
    vi.advanceTimersByTime(0);
    http.expectOne('/api/my/notifications/unread-count').flush(3);
    expect(service.unreadCount()).toBe(3);
    currentUser.set(null);
    TestBed.tick();
    expect(service.unreadCount()).toBe(0);
    vi.advanceTimersByTime(30000);
    http.expectNone('/api/my/notifications/unread-count');
  });

  it('cancels an old account request when switching accounts', () => {
    currentUser.set(user);
    TestBed.tick();
    vi.advanceTimersByTime(0);
    const oldRequest = http.expectOne('/api/my/notifications/unread-count');
    currentUser.set({ ...user, id: 2 });
    TestBed.tick();
    expect(oldRequest.cancelled).toBe(true);
    vi.advanceTimersByTime(0);
    http.expectOne('/api/my/notifications/unread-count').flush(1);
    expect(service.unreadCount()).toBe(1);
  });
});

import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AuthService, CurrentUser } from '../../features/auth/auth.service';
import { Home } from './home';

describe('Home', () => {
  const currentUser = signal<CurrentUser | null>(null);
  const sessionResolved = signal(true);
  let http: HttpTestingController;

  beforeEach(() => {
    currentUser.set(null);
    sessionResolved.set(true);
    TestBed.configureTestingModule({
      imports: [Home],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: { currentUser, sessionResolved } },
      ],
    });
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('shows sign-in benefits to visitors without loading private data', () => {
    const fixture = TestBed.createComponent(Home);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Zaloguj się');
    http.expectNone('/api/my/home');
    http.expectNone('/api/my/dogs');
  });

  it('shows school activity and personal bookings for a co-manager', () => {
    currentUser.set({ id: 1, email: 'a@example.test', displayName: 'Ala Kowalska', role: 'USER' });
    const fixture = TestBed.createComponent(Home);
    fixture.detectChanges();
    http.expectOne('/api/my/home').flush({
      managesSchools: true,
      pendingInvitations: 0,
      schoolTrainings: [
        {
          trainingId: 7,
          title: 'Psie przedszkole',
          startAt: '2030-01-02T10:00:00',
          capacity: 8,
          bookedCount: 2,
          schoolId: 1,
          schoolName: 'Szkoła',
        },
      ],
      recentRegistrations: [
        {
          trainingId: 7,
          trainingTitle: 'Psie przedszkole',
          schoolName: 'Szkoła',
          ownerName: 'Ola',
          dogName: 'Azor',
          createdAt: '2029-12-01T10:00:00',
        },
      ],
      personalBookings: [
        {
          bookingId: 5,
          trainingId: 8,
          trainingTitle: 'Spacer',
          dogName: 'Reks',
          startAt: '2030-01-03T10:00:00',
        },
      ],
    });
    http.expectOne('/api/my/dogs').flush([{ id: 1, name: 'Reks', images: [] }]);
    http
      .expectOne((request) => request.url === '/api/query/news')
      .flush({ items: [], page: 0, totalPages: 0 });
    fixture.detectChanges();
    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Cześć, Ala');
    expect(text).toContain('Psie przedszkole');
    expect(text).toContain('Ola i Azor');
    expect(text).toContain('Spacer');
    expect(text).toContain('Reks');
  });

  it('shows a useful empty state and pending invitation for a dog caregiver', () => {
    currentUser.set({ id: 1, email: 'a@example.test', displayName: 'Ala', role: 'USER' });
    const fixture = TestBed.createComponent(Home);
    fixture.detectChanges();
    http
      .expectOne('/api/my/home')
      .flush({
        managesSchools: false,
        pendingInvitations: 1,
        personalBookings: [],
        schoolTrainings: [],
        recentRegistrations: [],
      });
    http.expectOne('/api/my/dogs').flush([]);
    http
      .expectOne((request) => request.url === '/api/query/news')
      .flush({ items: [], page: 0, totalPages: 0 });
    fixture.detectChanges();
    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Masz nowe zaproszenie');
    expect(text).toContain('Nie macie jeszcze zaplanowanych treningów');
    expect(text).toContain('Dodaj psa');
    expect(text).not.toContain('Nowe zapisy');
  });

  it('shows an error instead of pretending failed data is empty', () => {
    currentUser.set({ id: 1, email: 'a@example.test', displayName: 'Ala', role: 'USER' });
    const fixture = TestBed.createComponent(Home);
    fixture.detectChanges();
    http.expectOne('/api/my/home').flush({}, { status: 500, statusText: 'Error' });
    http.expectOne('/api/my/dogs').flush([], { status: 500, statusText: 'Error' });
    http
      .expectOne((request) => request.url === '/api/query/news')
      .flush({ items: [], page: 0, totalPages: 0 });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Nie udało się pobrać Twoich treningów');
    expect(fixture.nativeElement.textContent).toContain('Nie udało się pobrać psów');
    expect(fixture.nativeElement.textContent).not.toContain(
      'Nie macie jeszcze zaplanowanych treningów',
    );
  });
});

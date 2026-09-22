import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { AuthService, CurrentUser } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('shares the initial session request with route guards', () => {
    const currentUser: CurrentUser = {
      id: 1,
      email: 'luna@example.com',
      displayName: 'Luna',
      role: 'USER',
    };
    const resolvedUsers: Array<CurrentUser | null> = [];

    service.resolveSession$().subscribe((user) => resolvedUsers.push(user));

    http.expectOne('/api/auth/csrf').flush({});
    const me = http.expectOne('/api/auth/me');
    me.flush(currentUser);

    expect(resolvedUsers).toEqual([currentUser]);
    expect(service.currentUser()).toEqual(currentUser);
    expect(service.sessionResolved()).toBe(true);
  });
});

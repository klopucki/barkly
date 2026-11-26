import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { AuthService } from '../../features/auth/auth.service';
import { Me } from './me';

describe('account invitations', () => {
  it('accepts an existing school invitation and removes it from the list', () => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: { currentUser: signal(null) } },
      ],
    });
    const page = TestBed.runInInjectionContext(() => new Me());
    const http = TestBed.inject(HttpTestingController);
    const invitation = {
      id: 5,
      type: 'SCHOOL' as const,
      resourceId: 12,
      resourceName: 'Szkoła',
      inviteeId: 3,
      inviteeName: 'User',
      status: 'PENDING' as const,
    };
    page.invitations.set([invitation]);
    page.respond(invitation, true);
    const request = http.expectOne('/api/access/school-invitations/5/accept');
    expect(request.request.method).toBe('POST');
    request.flush(null);
    expect(page.invitations()).toEqual([]);
    http.verify();
  });
});

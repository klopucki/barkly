import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { SharedAccess } from './shared-access';

describe('SharedAccess', () => {
  let http: HttpTestingController;
  let page: SharedAccess;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    page = TestBed.createComponent(SharedAccess).componentInstance;
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  function flushRefresh(): void {
    http.expectOne('/api/my/dogs').flush([]);
    http.expectOne('/api/my/schools').flush([]);
    http.expectOne('/api/access/invitations').flush([]);
  }

  it('invites an existing user to a selected dog', () => {
    page.type = 'DOG';
    page.resourceId = 4;
    page.email = ' user@example.com ';
    page.invite();
    const request = http.expectOne('/api/access/dogs/4');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ email: 'user@example.com' });
    request.flush(null);
    http.expectOne('/api/access/dogs/4').flush([]);
    expect(page.message()).toContain('Zaproszenie');
  });

  it('accepts a school invitation and refreshes access', () => {
    page.accept({
      id: 2,
      type: 'SCHOOL',
      resourceId: 3,
      resourceName: 'Szkoła',
      inviteeId: 4,
      inviteeName: 'User',
      status: 'PENDING',
    });
    const request = http.expectOne('/api/access/school-invitations/2/accept');
    expect(request.request.method).toBe('POST');
    request.flush(null);
    flushRefresh();
    expect(page.message()).toContain('przyjęte');
  });
});

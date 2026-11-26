import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ManageAccess } from './manage-access';

describe('contextual access management', () => {
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it.each([
    ['DOG', 'dogs'],
    ['SCHOOL', 'schools'],
  ] as const)('invites and revokes access for the selected %s', (type, path) => {
    const fixture = TestBed.createComponent(ManageAccess);
    fixture.componentRef.setInput('type', type);
    fixture.componentRef.setInput('resourceId', 42);
    fixture.detectChanges();
    http.expectOne(`/api/access/${path}/42`).flush([]);
    fixture.componentInstance.email = ' user@example.com ';
    fixture.componentInstance.invite();
    const invite = http.expectOne(`/api/access/${path}/42`);
    expect(invite.request.body).toEqual({ email: 'user@example.com' });
    invite.flush(null);
    const member = {
      id: 7,
      type,
      resourceId: 42,
      resourceName: 'Profil',
      inviteeId: 3,
      inviteeName: 'User',
      status: 'PENDING' as const,
    };
    http.expectOne(`/api/access/${path}/42`).flush([member]);
    fixture.componentInstance.revoke(member);
    http.expectOne(`/api/access/${type === 'DOG' ? 'dog' : 'school'}-invitations/7`).flush(null);
    http.expectOne(`/api/access/${path}/42`).flush([]);
    expect(fixture.componentInstance.members()).toEqual([]);
  });
});

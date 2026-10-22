import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { AuthService, CurrentUser } from '../../features/auth/auth.service';
import { AdminUsers } from './admin-users';

const admin: CurrentUser = {
  id: 1,
  email: 'admin@example.com',
  displayName: 'Admin',
  role: 'SUPER_ADMIN',
};
const user = {
  id: 2,
  email: 'user@example.com',
  displayName: 'User',
  role: 'USER' as const,
  active: true,
};

describe('AdminUsers', () => {
  let http: HttpTestingController;
  let page: AdminUsers;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: { currentUser: signal(admin) } },
      ],
    });
    page = TestBed.createComponent(AdminUsers).componentInstance;
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('searches and preserves applied search when changing pages', () => {
    page.query = ' user ';
    page.search();
    const request = http.expectOne((r) => r.url === '/api/admin/users');
    expect(request.request.params.get('query')).toBe('user');
    expect(request.request.params.get('page')).toBe('0');
    request.flush({ items: [user], page: 0, size: 20, totalItems: 21, totalPages: 2 });
    page.query = 'unsent';
    page.load(1);
    const next = http.expectOne((r) => r.url === '/api/admin/users');
    expect(next.request.params.get('query')).toBe('user');
    expect(next.request.params.get('page')).toBe('1');
    next.flush({ items: [], page: 1, size: 20, totalItems: 21, totalPages: 2 });
  });

  it('does not allow editing own account', () => {
    page.edit({ ...admin, active: true });
    expect(page.selected()).toBeNull();
    http.expectNone('/api/admin/users/1');
  });

  it('only saves after confirmation and updates the displayed account', () => {
    page.result.set({ items: [user], page: 0, size: 20, totalItems: 1, totalPages: 1 });
    page.edit(user);
    page.active = false;
    page.role = 'SCHOOL_ADMIN';
    http.expectNone('/api/admin/users/2');
    page.save();
    const request = http.expectOne('/api/admin/users/2');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({ role: 'SCHOOL_ADMIN', active: false });
    request.flush({ ...user, role: 'SCHOOL_ADMIN', active: false });
    expect(page.result()?.items[0].active).toBe(false);
    expect(page.selected()).toBeNull();
    expect(page.success()).toContain('User');
  });

  it('keeps the editor open and reports rejected changes', () => {
    page.edit(user);
    page.save();
    http.expectOne('/api/admin/users/2').flush({}, { status: 403, statusText: 'Forbidden' });
    expect(page.selected()?.id).toBe(2);
    expect(page.saving()).toBe(false);
    expect(page.error()).toContain('Brak dostępu');
  });
});

import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export type UserRole = 'USER' | 'SCHOOL_ADMIN' | 'SUPER_ADMIN';
export interface CurrentUser { id: number; email: string; displayName: string; role: UserRole; }

@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly currentUser = signal<CurrentUser | null>(null);
  constructor(private readonly http: HttpClient) {
    localStorage.removeItem('barkly-credentials');
    this.http.get('/api/auth/csrf').subscribe({ next: () => this.loadMe(), error: () => this.loadMe() });
  }
  register$(payload: { email: string; password: string; displayName: string; role: 'USER' | 'SCHOOL_ADMIN' }): Observable<CurrentUser> {
    return this.http.post<CurrentUser>('/api/auth/register', payload);
  }
  login$(email: string, password: string): Observable<CurrentUser> { return this.http.post<CurrentUser>('/api/auth/login', { email, password }).pipe(tap(user => this.currentUser.set(user))); }
  me$(): Observable<CurrentUser> { return this.http.get<CurrentUser>('/api/auth/me').pipe(tap(user => this.currentUser.set(user))); }
  loadMe(): void { this.me$().subscribe({ error: () => this.currentUser.set(null) }); }
  logout(): void { this.currentUser.set(null); this.http.post('/api/auth/logout', {}).subscribe(); }
}

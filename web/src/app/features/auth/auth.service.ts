import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, finalize, Observable, of, shareReplay, tap } from 'rxjs';

export type UserRole = 'USER' | 'SCHOOL_ADMIN' | 'SUPER_ADMIN';
export interface CurrentUser {
  id: number;
  email: string;
  displayName: string;
  role: UserRole;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly currentUser = signal<CurrentUser | null>(null);
  readonly sessionResolved = signal(false);
  private sessionCheck$?: Observable<CurrentUser | null>;

  constructor(private readonly http: HttpClient) {
    localStorage.removeItem('barkly-credentials');
    this.http.get('/api/auth/csrf').subscribe({ error: () => undefined });
    this.resolveSession$().subscribe();
  }
  register$(payload: {
    email: string;
    password: string;
    displayName: string;
    role: 'USER' | 'SCHOOL_ADMIN';
  }): Observable<CurrentUser> {
    return this.http.post<CurrentUser>('/api/auth/register', payload);
  }
  login$(email: string, password: string): Observable<CurrentUser> {
    return this.http.post<CurrentUser>('/api/auth/login', { email, password }).pipe(
      tap((user) => {
        this.currentUser.set(user);
        this.sessionResolved.set(true);
      }),
    );
  }
  me$(): Observable<CurrentUser> {
    return this.http
      .get<CurrentUser>('/api/auth/me')
      .pipe(tap((user) => this.currentUser.set(user)));
  }
  loadMe(): void {
    this.resolveSession$().subscribe();
  }
  resolveSession$(): Observable<CurrentUser | null> {
    if (this.sessionResolved()) {
      return of(this.currentUser());
    }
    if (!this.sessionCheck$) {
      this.sessionCheck$ = this.me$().pipe(
        catchError(() => {
          this.currentUser.set(null);
          return of(null);
        }),
        finalize(() => this.sessionResolved.set(true)),
        shareReplay({ bufferSize: 1, refCount: false }),
      );
    }
    return this.sessionCheck$;
  }
  logout(): void {
    this.currentUser.set(null);
    this.sessionResolved.set(true);
    this.http.post('/api/auth/logout', {}).subscribe();
  }
}

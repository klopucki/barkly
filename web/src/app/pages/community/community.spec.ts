import { ComponentFixture, TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { AuthService, CurrentUser } from '../../features/auth/auth.service';
import { DogService } from '../../features/dogs/dog.service';
import { Community } from './community';

describe('Community', () => {
  const currentUser = signal<CurrentUser | null>(null);
  const posts = vi.fn(() => of([]));
  const mine = vi.fn(() => of([]));
  let fixture: ComponentFixture<Community>;

  beforeEach(async () => {
    currentUser.set(null);
    posts.mockClear();
    mine.mockClear();
    await TestBed.configureTestingModule({
      imports: [Community],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: { currentUser } },
        { provide: DogService, useValue: { posts$: posts, mine$: mine } },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(Community);
  });

  it('invites guests to sign in without loading the feed, then loads it after sign-in', () => {
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Zaloguj się');
    expect(posts).not.toHaveBeenCalled();
    currentUser.set({ id: 1, displayName: 'Ala', email: 'a@example.com', role: 'USER' });
    fixture.detectChanges();
    expect(posts).toHaveBeenCalledOnce();
    expect(mine).toHaveBeenCalledOnce();
    expect(fixture.nativeElement.textContent).toContain('Zacznij rozmowę');
  });
});

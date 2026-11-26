import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from '../../features/auth/auth.service';
import { FavoritesService } from '../../shared/favorites.service';
import { News } from './news';

describe('News', () => {
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [News],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: { currentUser: signal(null) } },
        { provide: FavoritesService, useValue: { has: () => false, toggle: vi.fn() } },
      ],
    });
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('shows only same-origin stored images in article cards and loads more', () => {
    const fixture = TestBed.createComponent(News);
    fixture.detectChanges();
    const first = http.expectOne((request) => request.url === '/api/query/news');
    expect(first.request.params.get('page')).toBe('0');
    first.flush({
      items: [
        {
          id: 1,
          title: 'Artykuł',
          schoolName: 'Szkoła',
          authorDisplayName: 'Ala',
          content:
            '<img src="https://outside.test/track"><img src="/api/training-images/abc-123.jpg"><p>Treść</p>',
          publishedAt: '2030-01-01T00:00:00',
        },
        {
          id: 2,
          title: 'Bez zdjęcia',
          schoolName: 'Szkoła',
          authorDisplayName: 'Ola',
          content: '<img src="https://outside.test/image.jpg">',
          publishedAt: '2030-01-01T00:00:00',
        },
      ],
      page: 0,
      totalPages: 2,
    });
    fixture.detectChanges();
    const images = fixture.nativeElement.querySelectorAll('.journal-image img');
    expect(images.length).toBe(1);
    expect(images[0].getAttribute('src')).toBe('/api/training-images/abc-123.jpg');
    fixture.componentInstance.loadMore();
    const next = http.expectOne((request) => request.url === '/api/query/news');
    expect(next.request.params.get('page')).toBe('1');
    next.flush({ items: [], page: 1, totalPages: 2 });
    expect(fixture.componentInstance.items().length).toBe(2);
  });
});

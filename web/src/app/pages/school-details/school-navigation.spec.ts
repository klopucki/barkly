import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { AuthService } from '../../features/auth/auth.service';
import { SchoolDetails } from './school-details';

describe('school profile navigation', () => {
  it('loads the selected school when the route changes without recreating the component', () => {
    const params = new BehaviorSubject(convertToParamMap({ slug: 'first' }));
    const queryParams = new BehaviorSubject(convertToParamMap({}));
    const route = {
      paramMap: params,
      queryParamMap: queryParams,
      snapshot: { paramMap: params.value },
    };
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ActivatedRoute, useValue: route },
        { provide: AuthService, useValue: { currentUser: signal(null) } },
      ],
    });
    const page = TestBed.runInInjectionContext(() => new SchoolDetails());
    const http = TestBed.inject(HttpTestingController);
    page.ngOnInit();
    http.expectOne('/api/schools/first').flush({ id: 1, slug: 'first', name: 'First', images: [] });
    http.expectOne('/api/schools/1/news').flush([]);
    http.expectOne('/api/trainings').flush([]);
    expect(page.school()?.id).toBe(1);
    route.snapshot.paramMap = convertToParamMap({ slug: 'second' });
    params.next(route.snapshot.paramMap);
    expect(page.school()).toBeNull();
    http
      .expectOne('/api/schools/second')
      .flush({ id: 2, slug: 'second', name: 'Second', images: [] });
    http.expectOne('/api/schools/2/news').flush([]);
    http.expectOne('/api/trainings').flush([]);
    expect(page.school()?.id).toBe(2);
    http.verify();
  });
});

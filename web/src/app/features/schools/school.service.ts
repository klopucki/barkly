import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { School, SchoolNews, SchoolPayload } from './school.model';
import { PageResult } from '../../shared/page-result';
@Injectable({ providedIn: 'root' })
export class SchoolService {
  private readonly http = inject(HttpClient);
  all$(): Observable<School[]> {
    return this.http.get<School[]>('/api/schools');
  }
  search$(
    query: string,
    page = 0,
    size = 20,
    favoritesOnly = false,
  ): Observable<PageResult<School>> {
    return this.http.get<PageResult<School>>('/api/query/schools', {
      params: { query, page, size, favoritesOnly },
    });
  }
  mine$(): Observable<School[]> {
    return this.http.get<School[]>('/api/my/schools');
  }
  bySlug$(slug: string): Observable<School> {
    return this.http.get<School>(`/api/schools/${slug}`);
  }
  create$(body: SchoolPayload): Observable<School> {
    return this.http.post<School>('/api/schools', body);
  }
  update$(id: number, body: SchoolPayload): Observable<School> {
    return this.http.put<School>(`/api/schools/${id}`, body);
  }
  news$(id: number): Observable<SchoolNews[]> {
    return this.http.get<SchoolNews[]>(`/api/schools/${id}/news`);
  }
  managedNews$(id: number): Observable<SchoolNews[]> {
    return this.http.get<SchoolNews[]>(`/api/my/schools/${id}/news`);
  }
  newsById$(id: number): Observable<SchoolNews> {
    return this.http.get<SchoolNews>(`/api/news/${id}`);
  }
  searchNews$(
    query: string,
    page = 0,
    size = 20,
    favoritesOnly = false,
  ): Observable<PageResult<SchoolNews>> {
    return this.http.get<PageResult<SchoolNews>>('/api/query/news', {
      params: { query, page, size, favoritesOnly },
    });
  }
  addNews$(
    id: number,
    body: { title: string; content: string; active: boolean },
  ): Observable<SchoolNews> {
    return this.http.post<SchoolNews>(`/api/schools/${id}/news`, body);
  }
  updateNews$(
    id: number,
    body: { title: string; content: string; active: boolean },
  ): Observable<SchoolNews> {
    return this.http.put<SchoolNews>(`/api/news/${id}`, body);
  }
  uploadImage$(id: number, image: File) {
    const data = new FormData();
    data.append('image', image);
    return this.http.post(`/api/schools/${id}/images`, data);
  }
}

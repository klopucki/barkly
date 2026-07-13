import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { School, SchoolNews, SchoolPayload } from './school.model';
@Injectable({ providedIn: 'root' })
export class SchoolService {
  private readonly http = inject(HttpClient);
  all$(): Observable<School[]> { return this.http.get<School[]>('/api/schools'); }
  mine$(): Observable<School[]> { return this.http.get<School[]>('/api/my/schools'); }
  bySlug$(slug: string): Observable<School> { return this.http.get<School>(`/api/schools/${slug}`); }
  create$(body: SchoolPayload): Observable<School> { return this.http.post<School>('/api/schools', body); }
  news$(id: number): Observable<SchoolNews[]> { return this.http.get<SchoolNews[]>(`/api/schools/${id}/news`); }
  managedNews$(id: number): Observable<SchoolNews[]> { return this.http.get<SchoolNews[]>(`/api/my/schools/${id}/news`); }
  allNews$(): Observable<SchoolNews[]> { return this.http.get<SchoolNews[]>('/api/news'); }
  addNews$(id: number, body: { title: string; content: string; active: boolean }): Observable<SchoolNews> { return this.http.post<SchoolNews>(`/api/schools/${id}/news`, body); }
  updateNews$(id: number, body: { title: string; content: string; active: boolean }): Observable<SchoolNews> { return this.http.put<SchoolNews>(`/api/news/${id}`, body); }
  uploadImage$(id: number, image: File) { const data = new FormData(); data.append('image', image); return this.http.post(`/api/schools/${id}/images`, data); }
}

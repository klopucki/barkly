import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Dog,
  DogImage,
  DogPayload,
  DogPost,
  DogPostComment,
  DogVisibility,
  PostReactionType,
} from './dog.model';

@Injectable({ providedIn: 'root' })
export class DogService {
  private readonly http = inject(HttpClient);

  all$(): Observable<Dog[]> {
    return this.http.get<Dog[]>('/api/dogs');
  }

  mine$(): Observable<Dog[]> {
    return this.http.get<Dog[]>('/api/my/dogs');
  }

  one$(id: number): Observable<Dog> {
    return this.http.get<Dog>(`/api/dogs/${id}`);
  }

  create$(body: DogPayload): Observable<Dog> {
    return this.http.post<Dog>('/api/dogs', body);
  }

  update$(id: number, body: DogPayload): Observable<Dog> {
    return this.http.put<Dog>(`/api/dogs/${id}`, body);
  }

  delete$(id: number): Observable<void> {
    return this.http.delete<void>(`/api/dogs/${id}`);
  }

  uploadImage$(id: number, image: File, visibility: DogVisibility): Observable<DogImage> {
    const form = new FormData();
    form.append('image', image);
    form.append('visibility', visibility);
    return this.http.post<DogImage>(`/api/dogs/${id}/images`, form);
  }

  deleteImage$(id: number): Observable<void> {
    return this.http.delete<void>(`/api/dog-images/${id}`);
  }

  posts$(): Observable<DogPost[]> {
    return this.http.get<DogPost[]>('/api/dog-posts');
  }

  createPost$(dogId: number, content: string, image: File | null): Observable<DogPost> {
    const form = new FormData();
    form.append('dogId', String(dogId));
    form.append('content', content);
    if (image) form.append('image', image);
    return this.http.post<DogPost>('/api/dog-posts', form);
  }

  reactToPost$(id: number, reactionType: PostReactionType): Observable<DogPost> {
    return this.http.put<DogPost>(`/api/dog-posts/${id}/reaction`, { reactionType });
  }

  comments$(id: number): Observable<DogPostComment[]> {
    return this.http.get<DogPostComment[]>(`/api/dog-posts/${id}/comments`);
  }

  addComment$(id: number, content: string): Observable<DogPostComment> {
    return this.http.post<DogPostComment>(`/api/dog-posts/${id}/comments`, { content });
  }

  deleteComment$(id: number): Observable<void> {
    return this.http.delete<void>(`/api/dog-post-comments/${id}`);
  }
}

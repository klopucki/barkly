import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../features/auth/auth.service';
import {
  Dog,
  DogPost,
  DogPostComment,
  PostReactionType,
  dogPostImageUrl,
} from '../../features/dogs/dog.model';
import { DogService } from '../../features/dogs/dog.service';
import { GalleryImage, ImageGallery } from '../../shared/components/image-gallery/image-gallery';

@Component({
  selector: 'app-community',
  imports: [DatePipe, FormsModule, RouterLink, ImageGallery],
  templateUrl: './community.html',
})
export class Community implements OnInit {
  private readonly dogsApi = inject(DogService);
  protected readonly auth = inject(AuthService);
  readonly posts = signal<DogPost[]>([]);
  readonly dogs = signal<Dog[]>([]);
  selectedDogId: number | null = null;
  content = '';
  image: File | null = null;
  error = '';
  saving = false;
  readonly commentsByPost = signal<Record<number, DogPostComment[]>>({});
  readonly expandedComments = signal<Set<number>>(new Set());
  readonly commentDrafts = signal<Record<number, string>>({});
  readonly sendingCommentFor = signal<number | null>(null);

  ngOnInit(): void {
    this.loadPosts();
    if (this.auth.currentUser()) {
      this.dogsApi.mine$().subscribe({ next: (dogs) => this.dogs.set(dogs) });
    }
  }

  loadPosts(): void {
    this.dogsApi.posts$().subscribe({
      next: (posts) => this.posts.set(posts),
      error: () => (this.error = 'Nie udało się pobrać aktualności.'),
    });
  }

  selectImage(event: Event): void {
    this.image = (event.target as HTMLInputElement).files?.[0] ?? null;
  }

  galleryImages(post: DogPost): GalleryImage[] {
    return post.hasImage
      ? [{ src: dogPostImageUrl(post.id), alt: `Zdjęcie psa ${post.dogName}` }]
      : [];
  }

  publish(): void {
    if (!this.selectedDogId || !this.content.trim()) {
      this.error = 'Wybierz psa i napisz krótką aktualność.';
      return;
    }
    this.saving = true;
    this.error = '';
    this.dogsApi.createPost$(this.selectedDogId, this.content.trim(), this.image).subscribe({
      next: (post) => {
        this.posts.update((posts) => [post, ...posts]);
        this.content = '';
        this.image = null;
        this.saving = false;
      },
      error: (e) => {
        this.error = e.error?.message ?? 'Nie udało się opublikować aktualności.';
        this.saving = false;
      },
    });
  }

  reactionCount(post: DogPost, type: PostReactionType): number {
    return post.reactions.find((reaction) => reaction.reactionType === type)?.count ?? 0;
  }

  react(post: DogPost, reactionType: PostReactionType): void {
    if (!this.auth.currentUser()) return;
    this.dogsApi.reactToPost$(post.id, reactionType).subscribe({
      next: (updated) =>
        this.posts.update((posts) =>
          posts.map((item) => (item.id === updated.id ? updated : item)),
        ),
      error: () => (this.error = 'Nie udało się zapisać reakcji.'),
    });
  }

  toggleComments(post: DogPost): void {
    if (this.expandedComments().has(post.id)) {
      this.expandedComments.update((ids) => {
        const next = new Set(ids);
        next.delete(post.id);
        return next;
      });
      return;
    }
    this.dogsApi.comments$(post.id).subscribe({
      next: (comments) => {
        this.commentsByPost.update((all) => ({ ...all, [post.id]: comments }));
        this.expandedComments.update((ids) => new Set([...ids, post.id]));
      },
      error: () => (this.error = 'Nie udało się pobrać komentarzy.'),
    });
  }

  setCommentDraft(postId: number, content: string): void {
    this.commentDrafts.update((drafts) => ({ ...drafts, [postId]: content }));
  }

  addComment(post: DogPost): void {
    const content = this.commentDrafts()[post.id]?.trim();
    if (!content || !this.auth.currentUser()) return;
    this.sendingCommentFor.set(post.id);
    this.dogsApi.addComment$(post.id, content).subscribe({
      next: (comment) => {
        this.commentsByPost.update((all) => ({
          ...all,
          [post.id]: [...(all[post.id] ?? []), comment],
        }));
        this.commentDrafts.update((drafts) => ({ ...drafts, [post.id]: '' }));
        this.posts.update((posts) =>
          posts.map((item) =>
            item.id === post.id ? { ...item, commentCount: item.commentCount + 1 } : item,
          ),
        );
        this.sendingCommentFor.set(null);
      },
      error: () => {
        this.error = 'Nie udało się dodać komentarza.';
        this.sendingCommentFor.set(null);
      },
    });
  }

  deleteComment(post: DogPost, comment: DogPostComment): void {
    this.dogsApi.deleteComment$(comment.id).subscribe({
      next: () => {
        this.commentsByPost.update((all) => ({
          ...all,
          [post.id]: (all[post.id] ?? []).filter((item) => item.id !== comment.id),
        }));
        this.posts.update((posts) =>
          posts.map((item) =>
            item.id === post.id
              ? { ...item, commentCount: Math.max(0, item.commentCount - 1) }
              : item,
          ),
        );
      },
    });
  }
}

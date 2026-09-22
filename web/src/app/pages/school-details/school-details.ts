import {
  Component,
  ElementRef,
  OnInit,
  ViewChild,
  computed,
  effect,
  inject,
  signal,
} from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { School, SchoolNews, schoolImageUrl } from '../../features/schools/school.model';
import { SchoolService } from '../../features/schools/school.service';
import { AuthService } from '../../features/auth/auth.service';
import { Training } from '../../features/trainings/training.model';
import { TrainingService } from '../../features/trainings/training.service';
import { GalleryImage, ImageGallery } from '../../shared/components/image-gallery/image-gallery';
import { Modal } from '../../shared/components/modal/modal';
import {
  TrainingForm,
  TrainingFormSubmission,
} from '../../features/trainings/components/training-form/training-form';
@Component({
  selector: 'app-school-details',
  imports: [DatePipe, FormsModule, RouterLink, ImageGallery, Modal, TrainingForm],
  templateUrl: './school-details.html',
  styleUrl: './school-details.css',
})
export class SchoolDetails implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly service = inject(SchoolService);
  private readonly auth = inject(AuthService);
  private readonly trainingsService = inject(TrainingService);
  readonly school = signal<School | null>(null);
  readonly news = signal<SchoolNews[]>([]);
  readonly canManage = signal(false);
  readonly trainings = signal<Training[]>([]);
  readonly activeTab = signal<'about' | 'trainings' | 'news'>('about');
  protected readonly imageUrl = schoolImageUrl;
  readonly galleryImages = computed<GalleryImage[]>(
    () =>
      this.school()?.images.map((image) => ({
        src: schoolImageUrl(image.imageKey),
        alt: this.school()!.name,
      })) ?? [],
  );
  articleTitle = '';
  articleContent = '';
  articleActive = true;
  editingArticle: SchoolNews | null = null;
  error = '';
  showArticleDialog = false;
  readonly isAddTrainingOpen = signal(false);
  readonly addTrainingError = signal<string | null>(null);
  @ViewChild('editor') private editor?: ElementRef<HTMLElement>;
  constructor() {
    effect(() => {
      const user = this.auth.currentUser();
      const school = this.school();
      if (!user || !school) {
        this.canManage.set(false);
        return;
      }
      this.service.mine$().subscribe({
        next: (mine) => {
          const manages = mine.some((item) => item.id === school.id);
          this.canManage.set(manages);
          if (manages) this.loadNews(true);
        },
        error: () => this.canManage.set(false),
      });
    });
  }
  ngOnInit() {
    const slug = this.route.snapshot.paramMap.get('slug')!;
    this.service.bySlug$(slug).subscribe({
      next: (s) => {
        this.school.set(s);
        this.loadNews();
        this.loadTrainings(s.id);
      },
    });
  }
  private loadNews(managed = false) {
    const school = this.school();
    if (!school) return;
    (managed ? this.service.managedNews$(school.id) : this.service.news$(school.id)).subscribe({
      next: (n) => this.news.set(n),
      error: () => (this.error = 'Nie udało się pobrać artykułów.'),
    });
  }
  private loadTrainings(schoolId: number) {
    this.trainingsService.getTrainings$().subscribe({
      next: (trainings) =>
        this.trainings.set(trainings.filter((training) => training.schoolId === schoolId)),
      error: () => this.trainings.set([]),
    });
  }
  setTab(tab: 'about' | 'trainings' | 'news') {
    this.activeTab.set(tab);
  }
  openAddTraining(): void {
    this.addTrainingError.set(null);
    this.isAddTrainingOpen.set(true);
  }
  closeAddTraining(): void {
    this.isAddTrainingOpen.set(false);
  }
  addTraining(submission: TrainingFormSubmission): void {
    this.addTrainingError.set(null);
    this.trainingsService.addTraining$(submission.training).subscribe({
      next: (saved) => {
        if (!submission.image) {
          this.finishAddingTraining(saved);
          return;
        }
        this.trainingsService.uploadTrainingImage$(saved.id, submission.image).subscribe({
          next: (withImage) => this.finishAddingTraining(withImage),
          error: () => {
            this.finishAddingTraining(saved);
            this.addTrainingError.set('Trening zapisany, ale nie udało się przesłać zdjęcia.');
          },
        });
      },
      error: (error) =>
        this.addTrainingError.set(error.error?.message ?? 'Nie udało się dodać treningu.'),
    });
  }
  private finishAddingTraining(training: Training): void {
    this.trainings.update((items) => [training, ...items]);
    this.closeAddTraining();
  }
  format(command: string, value?: string) {
    this.editor?.nativeElement.focus();
    document.execCommand(command, false, value);
    this.syncEditor();
  }
  addLink() {
    const url = window.prompt('Adres linku');
    if (url) this.format('createLink', url);
  }
  openArticleDialog(article?: SchoolNews) {
    this.error = '';
    this.editingArticle = article ?? null;
    this.articleTitle = article?.title ?? '';
    this.articleContent = article?.content ?? '';
    this.articleActive = article?.active ?? true;
    this.showArticleDialog = true;
    if (article)
      setTimeout(() => {
        if (this.editor) this.editor.nativeElement.innerHTML = this.articleContent;
      });
  }
  closeArticleDialog() {
    this.showArticleDialog = false;
  }
  syncEditor() {
    this.articleContent = this.editor?.nativeElement.innerHTML ?? '';
  }
  uploadArticleImage(event: Event) {
    const school = this.school();
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!school || !file) return;
    this.service.uploadImage$(school.id, file).subscribe({
      next: (image: any) => {
        const img = `<img src="${schoolImageUrl(image.imageKey)}" alt="" class="my-4 max-w-full rounded-lg"/>`;
        if (this.editor) {
          this.editor.nativeElement.focus();
          document.execCommand('insertHTML', false, img);
          this.syncEditor();
        }
        input.value = '';
      },
      error: () => (this.error = 'Nie udało się przesłać zdjęcia.'),
    });
  }
  publish() {
    const school = this.school();
    this.syncEditor();
    if (!school) return;
    if (this.articleTitle.trim().length < 3 || !this.articleContent.trim()) {
      this.error = 'Podaj tytuł i treść artykułu.';
      return;
    }
    const body = {
      title: this.articleTitle.trim(),
      content: this.articleContent,
      active: this.articleActive,
    };
    const request = this.editingArticle
      ? this.service.updateNews$(this.editingArticle.id, body)
      : this.service.addNews$(school.id, body);
    request.subscribe({
      next: (item) => {
        this.news.update((items) =>
          this.editingArticle
            ? items.map((existing) => (existing.id === item.id ? item : existing))
            : [item, ...items],
        );
        this.articleTitle = '';
        this.articleContent = '';
        this.articleActive = true;
        this.editingArticle = null;
        if (this.editor) this.editor.nativeElement.innerHTML = '';
        this.error = '';
        this.closeArticleDialog();
      },
      error: (e) => (this.error = e.error?.message ?? 'Nie udało się zapisać artykułu.'),
    });
  }
}

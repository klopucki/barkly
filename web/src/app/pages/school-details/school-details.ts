import {
  Component,
  DestroyRef,
  ElementRef,
  OnInit,
  HostListener,
  SecurityContext,
  ViewChild,
  computed,
  effect,
  inject,
  signal,
} from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { DomSanitizer } from '@angular/platform-browser';
import { switchMap } from 'rxjs';
import { ImageLightbox } from '../../shared/components/image-lightbox/image-lightbox';
import { ProfileEditor } from '../../shared/components/profile-editor/profile-editor';
import { ProfileTextPipe } from '../../shared/components/profile-editor/profile-text.pipe';
import { FormsModule } from '@angular/forms';
import {
  School,
  SchoolNews,
  schoolImageUrl,
  schoolProfileImageKey,
} from '../../features/schools/school.model';
import { SchoolService } from '../../features/schools/school.service';
import { MessageForm } from '../../features/notifications/message-form/message-form';
import { AuthService } from '../../features/auth/auth.service';
import { Training } from '../../features/trainings/training.model';
import { TrainingService } from '../../features/trainings/training.service';
import { TrainingCard } from '../../features/trainings/components/training-card/training-card';
import { FavoritesService } from '../../shared/favorites.service';
import { GalleryImage, ImageGallery } from '../../shared/components/image-gallery/image-gallery';
import { Modal } from '../../shared/components/modal/modal';
import { ManageAccess } from '../../features/access/manage-access';
import {
  TrainingForm,
  TrainingFormSubmission,
} from '../../features/trainings/components/training-form/training-form';
@Component({
  selector: 'app-school-details',
  imports: [
    DatePipe,
    FormsModule,
    RouterLink,
    ImageGallery,
    ImageLightbox,
    Modal,
    ManageAccess,
    TrainingForm,
    MessageForm,
    TrainingCard,
    ProfileEditor,
    ProfileTextPipe,
  ],
  templateUrl: './school-details.html',
  styleUrl: './school-details.css',
})
export class SchoolDetails implements OnInit {
  protected readonly favorites = inject(FavoritesService);
  private readonly http = inject(HttpClient);
  private readonly sanitizer = inject(DomSanitizer);
  readonly subscribed = signal(false);
  readonly subscriptionLoading = signal(false);
  readonly subscriptionError = signal('');
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly queryParams = toSignal(this.route.queryParamMap);
  private readonly service = inject(SchoolService);
  protected readonly auth = inject(AuthService);
  private readonly trainingsService = inject(TrainingService);
  readonly school = signal<School | null>(null);
  readonly news = signal<SchoolNews[]>([]);
  readonly canManage = signal(false);
  readonly managementResolved = signal(false);
  readonly galleryLoading = signal(false);
  readonly galleryError = signal('');
  readonly profileImageLoading = signal(false);
  readonly profileImageError = signal('');
  readonly enlargedImage = signal<string | null>(null);
  readonly historyImageLoading = signal(false);
  readonly historyImageError = signal('');
  readonly sentMessages = signal<
    { id: number; message: string; link: string; createdAt: string }[]
  >([]);
  readonly sentMessagesError = signal('');
  readonly sentMessagesPage = signal(0);
  readonly hasMoreSentMessages = signal(false);
  readonly loadingSentMessages = signal(false);
  readonly trainings = signal<Training[]>([]);
  readonly activeTab = signal<'about' | 'trainings' | 'news' | 'notifications'>('about');
  protected readonly imageUrl = schoolImageUrl;
  protected readonly profileImageKey = schoolProfileImageKey;
  readonly galleryImages = computed<GalleryImage[]>(
    () =>
      this.school()?.images.map((image) => ({
        src: schoolImageUrl(image.imageKey),
        alt: this.school()!.name,
      })) ?? [],
  );
  readonly articleImageLoading = signal(false);
  readonly articlePreview = signal(false);
  private articleEditorVersion = 0;
  private savedArticleRange: Range | null = null;
  readonly selectedArticleImage = signal<HTMLImageElement | null>(null);
  articleTitle = '';
  articleContent = '';
  articleActive = true;
  editingArticle: SchoolNews | null = null;
  error = '';
  showArticleDialog = false;
  showSchoolEdit = false;
  showSchoolAccess = false;
  savingSchool = false;
  schoolEditError = '';
  schoolForm = {
    name: '',
    address: '',
    krs: '',
    description: '',
    activities: '',
    history: '',
    pricing: '',
  };

  readonly isAddTrainingOpen = signal(false);
  readonly addTrainingError = signal<string | null>(null);
  @ViewChild('editor') private editor?: ElementRef<HTMLElement>;
  constructor() {
    effect(() => {
      if (
        this.queryParams()?.get('newArticle') !== '1' ||
        !this.school() ||
        this.school()?.slug !== this.route.snapshot.paramMap.get('slug') ||
        !this.managementResolved()
      )
        return;
      if (this.canManage()) {
        this.activeTab.set('news');
        this.openArticleDialog();
      }
      this.router.navigate([], {
        relativeTo: this.route,
        queryParams: { newArticle: null },
        queryParamsHandling: 'merge',
        replaceUrl: true,
      });
    });
    effect(() => {
      const user = this.auth.currentUser();
      const school = this.school();
      if (!user || !school) {
        this.canManage.set(false);
        this.managementResolved.set(!!school);
        return;
      }
      this.managementResolved.set(false);
      this.service.mine$().subscribe({
        next: (mine) => {
          if (this.school()?.id !== school.id || this.auth.currentUser()?.id !== user.id) return;
          const manages = mine.some((item) => item.id === school.id);
          this.canManage.set(manages);
          this.managementResolved.set(true);
          if (manages) {
            this.loadNews(true);
            this.loadSentMessages();
          }
        },
        error: () => {
          if (this.school()?.id !== school.id || this.auth.currentUser()?.id !== user.id) return;
          this.canManage.set(false);
          this.managementResolved.set(true);
        },
      });
    });
  }
  ngOnInit() {
    this.route.paramMap
      .pipe(
        switchMap((params) => {
          this.school.set(null);
          this.canManage.set(false);
          this.managementResolved.set(false);
          return this.service.bySlug$(params.get('slug')!);
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (s) => {
          this.school.set(s);
          this.loadNews();
          this.loadTrainings(s.id);
          if (this.auth.currentUser())
            this.http.get<boolean>(`/api/my/school-subscriptions/${s.id}`).subscribe({
              next: (value) => this.subscribed.set(value),
              error: () => this.subscriptionError.set('Nie udało się pobrać subskrypcji.'),
            });
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
  loadSentMessages(append = false): void {
    const school = this.school();
    if (!school || this.loadingSentMessages()) return;
    const page = append ? this.sentMessagesPage() + 1 : 0;
    this.loadingSentMessages.set(true);
    this.http
      .get<
        { id: number; message: string; link: string; createdAt: string }[]
      >(`/api/schools/${school.id}/messages`, { params: { page } })
      .subscribe({
        next: (messages) => {
          this.sentMessages.set(append ? [...this.sentMessages(), ...messages] : messages);
          this.sentMessagesPage.set(page);
          this.hasMoreSentMessages.set(messages.length === 30);
          this.loadingSentMessages.set(false);
          this.sentMessagesError.set('');
        },
        error: () => {
          this.loadingSentMessages.set(false);
          this.sentMessagesError.set('Nie udało się pobrać historii powiadomień.');
        },
      });
  }

  uploadGalleryImage(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    const school = this.school();
    if (!school || !file || this.galleryLoading()) return;
    this.galleryLoading.set(true);
    this.galleryError.set('');
    this.service.uploadImage$(school.id, file, true).subscribe({
      next: (image) => {
        if (this.school()?.id === school.id)
          this.school.update((current) =>
            current ? { ...current, images: [...current.images, image] } : current,
          );
        this.galleryLoading.set(false);
        input.value = '';
      },
      error: () => {
        this.galleryError.set('Nie udało się dodać zdjęcia do galerii. Sprawdź format i rozmiar.');
        this.galleryLoading.set(false);
        input.value = '';
      },
    });
  }

  hideGalleryImage(imageId: number): void {
    const school = this.school();
    if (!school || this.galleryLoading()) return;
    this.galleryLoading.set(true);
    this.galleryError.set('');
    this.service.hideGalleryImage$(imageId).subscribe({
      next: (updated) => {
        if (this.school()?.id === school.id) this.school.set(updated);
        this.galleryLoading.set(false);
      },
      error: () => {
        this.galleryError.set('Nie udało się usunąć zdjęcia z galerii.');
        this.galleryLoading.set(false);
      },
    });
  }

  uploadProfileImage(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    const school = this.school();
    if (!file || !school || this.profileImageLoading()) return;
    this.profileImageLoading.set(true);
    this.profileImageError.set('');
    this.service
      .uploadImage$(school.id, file, false)
      .pipe(
        switchMap((image) =>
          this.http.put<School>(`/api/schools/${school.id}/profile-image/${image.id}`, {}),
        ),
      )
      .subscribe({
        next: (updated) => {
          this.school.set(updated);
          this.profileImageLoading.set(false);
          input.value = '';
        },
        error: () => {
          this.profileImageLoading.set(false);
          input.value = '';
          this.profileImageError.set(
            'Nie udało się ustawić zdjęcia. Sprawdź format i rozmiar, a potem spróbuj ponownie.',
          );
        },
      });
  }

  uploadHistoryImage(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    const school = this.school();
    if (!file || !school || this.historyImageLoading()) return;
    this.historyImageLoading.set(true);
    this.historyImageError.set('');
    this.service.uploadImage$(school.id, file, false).subscribe({
      next: (result) => {
        const imageId = (result as { id: number }).id;
        this.http.put<School>(`/api/schools/${school.id}/history-image/${imageId}`, {}).subscribe({
          next: (updated) => {
            this.school.set(updated);
            this.historyImageLoading.set(false);
            input.value = '';
          },
          error: () => {
            this.historyImageLoading.set(false);
            this.historyImageError.set(
              'Zdjęcie przesłano, ale nie udało się przypisać go do historii. Spróbuj ponownie.',
            );
            input.value = '';
          },
        });
      },
      error: () => {
        this.historyImageLoading.set(false);
        this.historyImageError.set(
          'Nie udało się dodać zdjęcia. Wybierz JPG lub PNG zgodny z wymaganiami.',
        );
        input.value = '';
      },
    });
  }

  removeHistoryImage(): void {
    const school = this.school();
    if (!school || this.historyImageLoading()) return;
    this.historyImageLoading.set(true);
    this.http.delete<School>(`/api/schools/${school.id}/history-image`).subscribe({
      next: (updated) => {
        this.school.set(updated);
        this.historyImageLoading.set(false);
        this.historyImageError.set('');
      },
      error: () => {
        this.historyImageLoading.set(false);
        this.historyImageError.set('Nie udało się usunąć zdjęcia z historii.');
      },
    });
  }

  openSchoolEdit(): void {
    const school = this.school();
    if (!school || !this.canManage()) return;
    this.schoolForm = {
      name: school.name,
      address: school.address,
      krs: school.krs ?? '',
      description: [school.description, school.activities]
        .filter((value) => value.trim())
        .join('\n\n'),
      activities: '',
      history: school.history ?? '',
      pricing: school.pricing,
    };
    this.schoolEditError = '';
    this.showSchoolEdit = true;
  }

  saveSchool(): void {
    const school = this.school();
    if (!school || this.savingSchool) return;
    this.savingSchool = true;
    this.schoolEditError = '';
    const form = this.schoolForm;
    this.service
      .update$(school.id, {
        name: form.name.trim(),
        address: form.address.trim(),
        krs: form.krs.trim() || null,
        description: form.description.trim(),
        activities: form.activities.trim(),
        history: form.history.trim(),
        pricing: form.pricing,
      })
      .subscribe({
        next: (updated) => {
          this.school.set(updated);
          this.savingSchool = false;
          this.showSchoolEdit = false;
        },
        error: () => {
          this.savingSchool = false;
          this.schoolEditError = 'Nie udało się zapisać profilu. Sprawdź dane i spróbuj ponownie.';
        },
      });
  }

  toggleSubscription(): void {
    const school = this.school();
    if (!school || this.subscriptionLoading()) return;
    this.subscriptionLoading.set(true);
    this.subscriptionError.set('');
    const subscribed = this.subscribed();
    const url = `/api/my/school-subscriptions/${school.id}`;
    (subscribed ? this.http.delete<void>(url) : this.http.put<void>(url, {})).subscribe({
      next: () => {
        this.subscribed.set(!subscribed);
        this.subscriptionLoading.set(false);
      },
      error: () => {
        this.subscriptionLoading.set(false);
        this.subscriptionError.set('Nie udało się zmienić subskrypcji. Spróbuj ponownie.');
      },
    });
  }
  setTab(tab: 'about' | 'trainings' | 'news' | 'notifications') {
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
    this.loadTrainings(training.schoolId);
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
    this.articleEditorVersion++;
    this.savedArticleRange = null;
    this.selectedArticleImage.set(null);
    this.articleImageLoading.set(false);
    this.articlePreview.set(false);
    this.showArticleDialog = true;
    if (article)
      setTimeout(() => {
        if (this.editor)
          this.editor.nativeElement.innerHTML =
            this.sanitizer.sanitize(SecurityContext.HTML, this.articleContent) ?? '';
      });
  }
  closeArticleDialog() {
    this.articleEditorVersion++;
    this.savedArticleRange = null;
    this.selectedArticleImage.set(null);
    this.articleImageLoading.set(false);
    this.showArticleDialog = false;
  }
  @HostListener('document:selectionchange')
  rememberEditorSelection(): void {
    const editor = this.editor?.nativeElement;
    const selection = window.getSelection();
    if (
      editor &&
      selection?.rangeCount &&
      editor.contains(selection.anchorNode) &&
      editor.contains(selection.focusNode)
    ) {
      this.savedArticleRange = selection.getRangeAt(0).cloneRange();
    }
  }

  selectEditorImage(event: MouseEvent): void {
    const editor = this.editor?.nativeElement;
    const target = event.target;
    const previous = this.selectedArticleImage();
    previous?.removeAttribute('data-article-selected');
    if (target instanceof HTMLImageElement && editor?.contains(target)) {
      target.setAttribute('data-article-selected', 'true');
      this.selectedArticleImage.set(target);
    } else {
      this.selectedArticleImage.set(null);
    }
  }

  removeSelectedArticleImage(): void {
    const image = this.selectedArticleImage();
    const editor = this.editor?.nativeElement;
    if (!image || !editor?.contains(image)) return;
    const range = document.createRange();
    range.setStartBefore(image);
    range.collapse(true);
    image.remove();
    this.selectedArticleImage.set(null);
    this.savedArticleRange = range;
    editor.focus();
    const selection = window.getSelection();
    selection?.removeAllRanges();
    selection?.addRange(range);
    this.syncEditor();
  }

  syncEditor() {
    const editor = this.editor?.nativeElement;
    if (!editor) {
      this.articleContent = '';
      return;
    }
    const copy = editor.cloneNode(true) as HTMLElement;
    copy
      .querySelectorAll('[data-article-selected]')
      .forEach((element) => element.removeAttribute('data-article-selected'));
    this.articleContent = copy.innerHTML;
    if (this.selectedArticleImage() && !editor.contains(this.selectedArticleImage()))
      this.selectedArticleImage.set(null);
  }
  pasteArticle(event: ClipboardEvent): void {
    const editor = this.editor?.nativeElement;
    const clipboard = event.clipboardData;
    if (!editor || !clipboard) return;
    event.preventDefault();
    const html = clipboard.getData('text/html');
    const container = document.createElement('div');
    if (html) {
      container.innerHTML = this.sanitizer.sanitize(SecurityContext.HTML, html) ?? '';
    } else {
      container.textContent = clipboard.getData('text/plain').replace(/\r\n?/g, '\n');
    }
    const selection = window.getSelection();
    const range =
      selection?.rangeCount && editor.contains(selection.anchorNode)
        ? selection.getRangeAt(0)
        : document.createRange();
    if (!editor.contains(range.commonAncestorContainer)) {
      range.selectNodeContents(editor);
      range.collapse(false);
    }
    range.deleteContents();
    const fragment = document.createDocumentFragment();
    while (container.firstChild) fragment.appendChild(container.firstChild);
    const last = fragment.lastChild;
    range.insertNode(fragment);
    if (last && selection) {
      range.setStartAfter(last);
      range.collapse(true);
      selection.removeAllRanges();
      selection.addRange(range);
    }
    this.syncEditor();
  }

  toggleArticlePreview(): void {
    this.syncEditor();
    this.articlePreview.update((preview) => !preview);
  }

  uploadArticleImage(event: Event) {
    const school = this.school();
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    const editor = this.editor?.nativeElement;
    if (!school || !file || !editor || this.articleImageLoading()) return;
    this.rememberEditorSelection();
    const range = this.savedArticleRange?.cloneRange() ?? null;
    const editorVersion = this.articleEditorVersion;
    this.error = '';
    this.articleImageLoading.set(true);
    this.service.uploadImage$(school.id, file, false).subscribe({
      next: (image) => {
        if (editorVersion !== this.articleEditorVersion) return;
        const img = document.createElement('img');
        img.src = schoolImageUrl(image.imageKey);
        img.alt = '';
        img.className = 'my-4 max-w-full rounded-lg';
        if (range && editor.contains(range.commonAncestorContainer)) {
          range.deleteContents();
          range.insertNode(img);
        } else {
          editor.appendChild(img);
        }
        const cursor = document.createRange();
        cursor.setStartAfter(img);
        cursor.collapse(true);
        this.savedArticleRange = cursor.cloneRange();
        editor.focus();
        const selection = window.getSelection();
        selection?.removeAllRanges();
        selection?.addRange(cursor);
        this.syncEditor();
        this.articleImageLoading.set(false);
        input.value = '';
      },
      error: (error) => {
        if (editorVersion !== this.articleEditorVersion) return;
        this.articleImageLoading.set(false);
        input.value = '';
        this.error =
          error.status === 413 || error.error?.image
            ? 'Zdjęcie jest za duże. Wybierz plik do 10 MB.'
            : error.status === 400
              ? 'Nie udało się przesłać zdjęcia. Wybierz JPG lub PNG o wymiarach co najmniej 640 × 480 px.'
              : 'Nie udało się przesłać zdjęcia. Spróbuj ponownie.';
      },
    });
  }
  publish() {
    if (this.articleImageLoading()) return;
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

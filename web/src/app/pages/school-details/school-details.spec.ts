import { ElementRef, signal } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from '../../features/auth/auth.service';
import { School } from '../../features/schools/school.model';
import { SchoolDetails } from './school-details';

describe('article editor', () => {
  let component: SchoolDetails;
  let editor: HTMLElement;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: { currentUser: signal(null) } },
      ],
    });
    component = TestBed.runInInjectionContext(() => new SchoolDetails());
    component.school.set({ id: 2, name: 'School', images: [] } as unknown as School);
    editor = document.createElement('div');
    editor.contentEditable = 'true';
    (component as unknown as { editor: ElementRef<HTMLElement> }).editor = new ElementRef(editor);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    editor.remove();
    window.getSelection()?.removeAllRanges();
    http.verify();
  });

  it('does not close the article editor when its backdrop is clicked', () => {
    const fixture = TestBed.createComponent(SchoolDetails);
    fixture.componentInstance.school.set({
      id: 2,
      name: 'School',
      images: [],
    } as unknown as School);
    fixture.componentInstance.openArticleDialog();
    fixture.detectChanges();
    http.expectOne('/api/schools/null').flush({ id: 2, name: 'School', images: [] });
    http.expectOne('/api/schools/2/news').flush([]);
    http.expectOne('/api/trainings').flush([]);
    fixture.detectChanges();
    const backdrop = fixture.nativeElement.querySelector(
      '[aria-label="Edytor artykułu"]',
    ) as HTMLElement;
    backdrop.click();
    fixture.detectChanges();
    expect(fixture.componentInstance.showArticleDialog).toBe(true);
  });

  it('adds and removes a gallery image without deleting the asset', () => {
    const input = document.createElement('input');
    Object.defineProperty(input, 'files', { value: [new File(['x'], 'gallery.jpg')] });
    component.uploadGalleryImage({ target: input } as unknown as Event);
    const upload = http.expectOne('/api/schools/2/images');
    expect((upload.request.body as FormData).get('gallery')).toBe('true');
    upload.flush({ id: 9, imageKey: 'gallery.jpg' });
    expect(component.school()?.images).toHaveLength(1);
    component.hideGalleryImage(9);
    const remove = http.expectOne('/api/school-images/9/gallery');
    expect(remove.request.method).toBe('DELETE');
    remove.flush({ id: 2, name: 'School', images: [] });
    expect(component.school()?.images).toHaveLength(0);
  });

  it('inserts an article image at the saved cursor after the file picker removes focus', () => {
    document.body.append(editor);
    editor.innerHTML = '<p>Before after</p>';
    const text = editor.querySelector('p')!.firstChild!;
    const range = document.createRange();
    range.setStart(text, 6);
    range.collapse(true);
    const selection = window.getSelection()!;
    selection.removeAllRanges();
    selection.addRange(range);
    component.rememberEditorSelection();
    selection.removeAllRanges();
    const input = document.createElement('input');
    Object.defineProperty(input, 'files', { value: [new File(['x'], 'photo.jpg')] });
    component.uploadArticleImage({ target: input } as unknown as Event);
    const upload = http.expectOne('/api/schools/2/images');
    expect((upload.request.body as FormData).get('gallery')).toBe('false');
    upload.flush({ id: 9, imageKey: 'photo.jpg' });
    const paragraph = editor.querySelector('p')!;
    expect(paragraph.childNodes[0].textContent).toBe('Before');
    expect(paragraph.childNodes[1].nodeName).toBe('IMG');
    expect(paragraph.childNodes[2].textContent).toBe(' after');
  });

  it('removes the selected article image and does not save its selection marker', () => {
    document.body.append(editor);
    editor.innerHTML = '<p>Text<img src="/api/training-images/example.jpg">more</p>';
    const image = editor.querySelector('img')!;
    component.selectEditorImage({ target: image } as unknown as MouseEvent);
    expect(component.selectedArticleImage()).toBe(image);
    component.syncEditor();
    expect(component.articleContent).not.toContain('data-article-selected');
    component.removeSelectedArticleImage();
    expect(editor.querySelector('img')).toBeNull();
    expect(component.articleContent).toContain('Textmore');
  });

  it('uploads and sets a profile image for the current school', () => {
    const input = document.createElement('input');
    Object.defineProperty(input, 'files', { value: [new File(['x'], 'photo.jpg')] });
    component.uploadProfileImage({ target: input } as unknown as Event);
    const upload = http.expectOne('/api/schools/2/images');
    expect((upload.request.body as FormData).get('gallery')).toBe('false');
    upload.flush({ id: 9, imageKey: 'photo.jpg' });
    const update = http.expectOne('/api/schools/2/profile-image/9');
    expect(update.request.method).toBe('PUT');
    update.flush({ id: 2, name: 'School', profileImageKey: 'photo.jpg', images: [] });
    expect(component.school()?.profileImageKey).toBe('photo.jpg');
    expect(component.profileImageLoading()).toBe(false);
  });

  it('uploads an image and saves it in article content even without an editor selection', () => {
    component.articleTitle = 'Article title';
    editor.innerHTML = '<p>First paragraph</p><p><br></p><p>Second paragraph</p>';
    const file = new File(['image'], 'photo.jpg', { type: 'image/jpeg' });
    const input = document.createElement('input');
    Object.defineProperty(input, 'files', { value: [file] });
    component.uploadArticleImage({ target: input } as unknown as Event);
    expect(component.articleImageLoading()).toBe(true);
    component.publish();
    http.expectNone('/api/schools/2/news');
    const upload = http.expectOne('/api/schools/2/images');
    expect((upload.request.body as FormData).get('image')).toBe(file);
    upload.flush({ id: 9, imageKey: 'photo.jpg' });
    expect(component.articleImageLoading()).toBe(false);
    component.publish();
    const save = http.expectOne('/api/schools/2/news');
    expect(save.request.body.content).toContain('<p><br></p>');
    expect(save.request.body.content).toContain('/api/training-images/photo.jpg');
    save.flush({ id: 1 });
  });

  it('reports upload errors and permits retry', () => {
    const input = document.createElement('input');
    Object.defineProperty(input, 'files', { value: [new File(['x'], 'x.png')] });
    component.uploadArticleImage({ target: input } as unknown as Event);
    http.expectOne('/api/schools/2/images').flush({}, { status: 400, statusText: 'Bad Request' });
    expect(component.articleImageLoading()).toBe(false);
    expect(component.error).toContain('JPG lub PNG');
  });

  it('sanitizes pasted HTML and preserves paragraphs and formatting', () => {
    const event = {
      preventDefault: vi.fn(),
      clipboardData: {
        getData: (type: string) =>
          type === 'text/html'
            ? '<p>First</p><p><br></p><p><strong>Bold</strong></p><img src="x" onerror="alert(1)">'
            : '',
      },
    } as unknown as ClipboardEvent;
    component.pasteArticle(event);
    expect(component.articleContent).toContain('<p><br></p>');
    expect(component.articleContent).toContain('<strong>Bold</strong>');
    expect(component.articleContent).not.toContain('onerror');
  });
});

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

  afterEach(() => http.verify());

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

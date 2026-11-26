import { TestBed } from '@angular/core/testing';
import { ImageLightbox } from './image-lightbox';

describe('ImageLightbox', () => {
  it('shows an image and closes on Escape', () => {
    const fixture = TestBed.createComponent(ImageLightbox);
    fixture.componentRef.setInput('src', '/api/training-images/photo.jpg');
    fixture.componentRef.setInput('alt', 'Szkoła');
    const closed = vi.fn();
    fixture.componentInstance.closed.subscribe(closed);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('img').getAttribute('src')).toBe(
      '/api/training-images/photo.jpg',
    );
    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    expect(closed).toHaveBeenCalledOnce();
  });
});

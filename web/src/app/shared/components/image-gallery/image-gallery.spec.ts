import { TestBed } from '@angular/core/testing';
import { ImageGallery } from './image-gallery';

describe('ImageGallery', () => {
  it('keeps the last remaining photo visible after the selected photo is removed', () => {
    const fixture = TestBed.createComponent(ImageGallery);
    fixture.componentRef.setInput('images', [
      { src: '/one.jpg', alt: 'One' },
      { src: '/two.jpg', alt: 'Two' },
    ]);
    fixture.detectChanges();
    fixture.componentInstance.select(1);
    fixture.detectChanges();
    expect(fixture.componentInstance.current()?.src).toBe('/two.jpg');
    fixture.componentRef.setInput('images', [{ src: '/one.jpg', alt: 'One' }]);
    fixture.detectChanges();
    expect(fixture.componentInstance.current()?.src).toBe('/one.jpg');
    expect(fixture.componentInstance.selectedIndex()).toBe(0);
  });
});

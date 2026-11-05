import { TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { LocationMap } from '../location-map';

export async function locationMapFixture() {
  await TestBed.configureTestingModule({ imports: [LocationMap] }).compileComponents();
  const fixture = TestBed.createComponent(LocationMap);
  fixture.componentRef.setInput('editable', true);
  const selected = vi.spyOn(fixture.componentInstance.selected, 'emit');
  fixture.detectChanges();
  await fixture.whenStable();
  return { fixture, selected };
}

export async function waitForMapReady(isReady: () => boolean): Promise<void> {
  await vi.waitFor(() => {
    if (!isReady()) throw new Error('Map not ready');
  });
}

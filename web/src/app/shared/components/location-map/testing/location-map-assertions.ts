import { expect } from 'vitest';
export function markerWasPlacedInOrder(calls: string[], coords: unknown, selected: any): void {
  expect(calls).toEqual(['setLngLat', 'addTo']);
  expect(coords).toEqual([21.01, 52.23]);
  expect(selected).toHaveBeenCalledWith({ latitude: 52.23, longitude: 21.01 });
}

import { expect } from 'vitest';
import { CardFixture } from './card-fixtures';

export const Assertions = {
  created(s: CardFixture) {
    expect(s.componentInstance).toBeTruthy();
  },
  priceToBeAgreed(s: CardFixture) {
    expect(s.nativeElement.textContent).toContain('Cena do ustalenia');
  },
  paidPrice(s: CardFixture) {
    expect(s.nativeElement.textContent).toContain('80,50');
    expect(s.nativeElement.textContent).toContain('/ pies');
  },
  freePrice(s: CardFixture) {
    expect(s.nativeElement.textContent).toContain('Bezpłatnie');
  },
  remainingCapacity(s: CardFixture) {
    expect(s.nativeElement.textContent).toContain('Wolne miejsca: 7 z 10');
  },
};

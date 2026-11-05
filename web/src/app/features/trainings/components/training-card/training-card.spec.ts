import { configureCardTests, Fixtures } from './testing/card-fixtures';
import { Assertions } from './testing/card-assertions';

describe('TrainingCard', () => {
  beforeEach(configureCardTests);

  it('creates the card', () => {
    const fixture = Fixtures.trainingCard();
    fixture.detectChanges();
    Assertions.created(fixture);
  });

  it('shows all price modes without an image or description', () => {
    const fixture = Fixtures.trainingCard();
    fixture.detectChanges();
    Assertions.priceToBeAgreed(fixture);

    fixture.componentRef.setInput('training', {
      ...fixture.componentInstance.training(),
      priceMode: 'PAID',
      price: 80.5,
    });
    fixture.detectChanges();
    Assertions.paidPrice(fixture);

    fixture.componentRef.setInput('training', {
      ...fixture.componentInstance.training(),
      priceMode: 'FREE',
      price: null,
    });
    fixture.detectChanges();
    Assertions.freePrice(fixture);
  });

  it('shows remaining capacity', () => {
    const fixture = Fixtures.trainingCard();
    fixture.detectChanges();
    Assertions.remainingCapacity(fixture);
  });
});

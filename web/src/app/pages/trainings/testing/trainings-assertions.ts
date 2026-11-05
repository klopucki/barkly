import { expect } from 'vitest';
import { TrainingListFixture } from './trainings-fixtures';

export function expectChronologicalList(fixture: TrainingListFixture, ids: number[]): void {
  expect(fixture.api.search$).toHaveBeenCalledExactlyOnceWith('', 0, 20, false);
  expect(fixture.component.trainings().map((training) => training.id)).toEqual(ids);
  expect(fixture.component.page()).toBe(0);
  expect(fixture.component.isAddTrainingOpen()).toBe(false);
}

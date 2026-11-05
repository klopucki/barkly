import { trainingListFixture } from './testing/trainings-fixtures';
import { expectChronologicalList } from './testing/trainings-assertions';

describe('Trainings ordering', () => {
  it.each([
    ['01', [3, 1, 2]],
    ['04', [1, 3, 2]],
    ['09', [1, 2, 3]],
  ] as const)('places a newly added training on day %s by its date', async (day, ids) => {
    const fixture = await trainingListFixture('add', day);

    fixture.component.saveTraining(fixture.submission);

    expectChronologicalList(fixture, [...ids]);
  });

  it('moves an edited training to its chronological position', async () => {
    const fixture = await trainingListFixture('edit', '01');

    fixture.component.saveTraining(fixture.submission);

    expectChronologicalList(fixture, [2, 1]);
  });
});

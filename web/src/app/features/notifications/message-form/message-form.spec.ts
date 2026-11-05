import { messageFixture } from './testing/message-fixtures';
import { confirmationWithoutSending, messageWasSent } from './testing/message-assertions';

describe('message confirmation', () => {
  it.each(['schools', 'trainings'] as const)('asks before notifying %s', async (audience) => {
    const fixture = await messageFixture(audience);
    fixture.component.openConfirmation();
    confirmationWithoutSending(fixture);
  });
  it('sends only after confirmation', async () => {
    const fixture = await messageFixture('schools');
    fixture.component.openConfirmation();
    fixture.component.send();
    messageWasSent(fixture);
  });
});

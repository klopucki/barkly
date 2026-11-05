import { expect } from 'vitest';
import { MessageFixture } from './message-fixtures';
export function confirmationWithoutSending(fixture: MessageFixture): void {
  expect(fixture.component.confirming()).toBe(true);
  fixture.http.expectNone(`/api/${fixture.audience}/7/messages`);
}
export function messageWasSent(fixture: MessageFixture): void {
  const request = fixture.http.expectOne(`/api/${fixture.audience}/7/messages`);
  expect(request.request.method).toBe('POST');
  expect(request.request.body).toEqual({ message: 'Ważna wiadomość' });
  request.flush(null);
  expect(fixture.component.confirming()).toBe(false);
  fixture.http.verify();
}

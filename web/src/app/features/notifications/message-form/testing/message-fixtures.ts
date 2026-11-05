import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { MessageForm } from '../message-form';

export async function messageFixture(audience: 'schools' | 'trainings') {
  await TestBed.configureTestingModule({
    imports: [MessageForm],
    providers: [provideHttpClient(), provideHttpClientTesting()],
  }).compileComponents();
  const fixture = TestBed.createComponent(MessageForm);
  fixture.componentRef.setInput('audience', audience);
  fixture.componentRef.setInput('id', 7);
  fixture.componentRef.setInput('label', 'Wiadomość');
  fixture.detectChanges();
  fixture.componentInstance.message = 'Ważna wiadomość';
  return {
    component: fixture.componentInstance,
    http: TestBed.inject(HttpTestingController),
    audience,
  };
}
export type MessageFixture = Awaited<ReturnType<typeof messageFixture>>;

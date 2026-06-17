import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-modal',
  imports: [],
  templateUrl: './modal.html',
})
export class Modal {
  title = input.required<string>();
  closed = output<void>();

  close(): void {
    this.closed.emit();
  }
}

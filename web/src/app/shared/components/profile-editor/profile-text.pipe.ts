import { Pipe, PipeTransform } from '@angular/core';

export function renderProfileText(value: string): string {
  return value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\*\*([^*\n]+)\*\*/g, '<strong>$1</strong>')
    .replace(/\*([^*\n]+)\*/g, '<em>$1</em>')
    .replace(/^### (.+)$/gm, '<strong>$1</strong>')
    .replace(/^- (.+)$/gm, '• $1')
    .replace(/\n/g, '<br>');
}

@Pipe({ name: 'profileText' })
export class ProfileTextPipe implements PipeTransform {
  transform(value: string | null | undefined): string {
    return renderProfileText(value ?? '');
  }
}

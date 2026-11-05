import { Component, ElementRef, forwardRef, input, ViewChild } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { renderProfileText } from './profile-text.pipe';

@Component({
  selector: 'app-profile-editor',
  providers: [
    { provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => ProfileEditor), multi: true },
  ],
  templateUrl: './profile-editor.html',
  styleUrl: './profile-editor.css',
})
export class ProfileEditor implements ControlValueAccessor {
  label = input.required<string>();
  placeholder = input('');
  maxLength = input(5000);
  @ViewChild('editor') editor?: ElementRef<HTMLTextAreaElement>;
  value = '';
  preview = false;
  disabled = false;
  private change: (value: string) => void = () => {};
  private touch: () => void = () => {};

  writeValue(value: string | null): void {
    this.value = value ?? '';
  }
  registerOnChange(fn: (value: string) => void): void {
    this.change = fn;
  }
  registerOnTouched(fn: () => void): void {
    this.touch = fn;
  }
  setDisabledState(disabled: boolean): void {
    this.disabled = disabled;
  }
  update(event: Event): void {
    this.value = (event.target as HTMLTextAreaElement).value;
    this.change(this.value);
  }
  touched(): void {
    this.touch();
  }
  format(prefix: string, suffix = ''): void {
    const field = this.editor?.nativeElement;
    if (!field || this.disabled) return;
    const start = field.selectionStart;
    const end = field.selectionEnd;
    const selected = this.value.slice(start, end) || 'tekst';
    const next = this.value.slice(0, start) + prefix + selected + suffix + this.value.slice(end);
    if (next.length > this.maxLength()) return;
    this.value = next;
    this.change(next);
    field.focus();
    requestAnimationFrame(() =>
      field.setSelectionRange(start + prefix.length, start + prefix.length + selected.length),
    );
  }
  get previewHtml(): string {
    return renderProfileText(this.value);
  }
}

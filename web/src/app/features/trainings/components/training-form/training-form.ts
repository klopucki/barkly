import { Component, output } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { Training, TrainingLevel } from '../../training.model';

export type TrainingFormValue = Omit<Training, 'id' | 'bookedCount'>;

@Component({
  selector: 'app-training-form',
  imports: [ReactiveFormsModule],
  templateUrl: './training-form.html',
})
export class TrainingForm {
  trainingSubmitted = output<TrainingFormValue>();
  cancelled = output<void>();

  private readonly fb = new FormBuilder();

  levels: TrainingLevel[] = ['PUPPY', 'BASIC', 'ADVANCED', 'SPORT'];

  form = this.fb.nonNullable.group({
    schoolId: [1, [Validators.required, Validators.min(1)]],
    title: [
      '',
      [Validators.required, Validators.pattern(/\S/), Validators.minLength(3), Validators.maxLength(200)],
    ],
    trainerName: [
      '',
      [Validators.required, Validators.pattern(/\S/), Validators.minLength(2), Validators.maxLength(200)],
    ],
    level: ['BASIC' as TrainingLevel, [Validators.required]],
    startAt: ['', [Validators.required, futureDateValidator]],
    capacity: [null as number | null, [Validators.min(1)]],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.trainingSubmitted.emit(this.form.getRawValue());
  }

  cancel(): void {
    this.cancelled.emit();
  }

  hasError(controlName: string, errorCode: string): boolean {
    const control = this.form.get(controlName);
    return !!control && control.touched && control.hasError(errorCode);
  }
}

function futureDateValidator(control: AbstractControl<string>): ValidationErrors | null {
  if (!control.value) {
    return null;
  }

  const date = new Date(control.value);
  return Number.isNaN(date.getTime()) || date.getTime() <= Date.now() ? { futureDate: true } : null;
}

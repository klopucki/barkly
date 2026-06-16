import { Component, output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
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

  levels: TrainingLevel[] = ['puppy', 'basic', 'advanced', 'sport'];

  form = this.fb.nonNullable.group({
    schoolId: [1, [Validators.required]],
    title: ['', [Validators.required, Validators.minLength(3)]],
    trainerName: ['', [Validators.required]],
    level: ['basic' as TrainingLevel, [Validators.required]],
    startAt: ['', [Validators.required]],
    capacity: [null as number | null],
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
}

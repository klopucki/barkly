import { Component, inject, input, OnInit, output } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import {
  DictionaryValue,
  TrainingCreatePayload,
  TrainingDictionaries,
  TrainingLevelDictionary,
  Training,
} from '../../training.model';
import { TrainingService } from '../../training.service';

export type TrainingFormValue = TrainingCreatePayload;

export interface TrainingFormSubmission {
  training: TrainingFormValue;
  image: File | null;
}

@Component({
  selector: 'app-training-form',
  imports: [ReactiveFormsModule],
  templateUrl: './training-form.html',
})
export class TrainingForm implements OnInit {
  trainingSubmitted = output<TrainingFormSubmission>();
  cancelled = output<void>();
  training = input<Training | null>(null);

  private readonly fb = new FormBuilder();
  private readonly trainingService = inject(TrainingService);

  dictionaries: TrainingDictionaries = {
    trainingTypes: [],
    trainingLevels: [],
    targetGroups: [],
  };
  selectedImage: File | null = null;
  imageError: string | null = null;

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
    trainingTypeId: [0, [Validators.required, Validators.min(1)]],
    trainingLevelId: [null as number | null],
    targetGroupId: [null as number | null],
    homeVisit: [false],
    startAt: ['', [Validators.required, futureDateValidator]],
    capacity: [null as number | null, [Validators.min(1)]],
  });

  ngOnInit(): void {
    this.trainingService.getTrainingDictionaries$().subscribe((dictionaries) => {
      this.dictionaries = dictionaries;
      if (dictionaries.trainingTypes.length > 0) {
        const training = this.training();
        if (training) {
          this.form.patchValue({
            schoolId: training.schoolId,
            title: training.title,
            trainerName: training.trainerName,
            trainingTypeId: training.trainingType.id,
            trainingLevelId: training.trainingLevel?.id ?? null,
            targetGroupId: training.targetGroup?.id ?? null,
            homeVisit: training.homeVisit,
            startAt: training.startAt.slice(0, 16),
            capacity: training.capacity,
          });
        } else {
          this.form.controls.trainingTypeId.setValue(dictionaries.trainingTypes[0].id);
        }
      }
    });
  }

  get availableLevels(): TrainingLevelDictionary[] {
    const typeId = this.form.controls.trainingTypeId.value;
    return this.dictionaries.trainingLevels.filter(
      (level) => level.trainingTypeId === null || level.trainingTypeId === typeId,
    );
  }

  get consultationSelected(): boolean {
    return this.selectedType()?.code === 'CONSULTATION';
  }

  get editing(): boolean {
    return this.training() !== null;
  }

  typeChanged(): void {
    const selectedLevel = this.form.controls.trainingLevelId.value;
    if (selectedLevel && !this.availableLevels.some((level) => level.id === selectedLevel)) {
      this.form.controls.trainingLevelId.setValue(null);
    }
    if (!this.consultationSelected) {
      this.form.controls.homeVisit.setValue(false);
    }
  }

  private selectedType(): DictionaryValue | undefined {
    return this.dictionaries.trainingTypes.find(
      (type) => type.id === this.form.controls.trainingTypeId.value,
    );
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.trainingSubmitted.emit({
      training: this.form.getRawValue(),
      image: this.selectedImage,
    });
  }

  cancel(): void {
    this.cancelled.emit();
  }

  hasError(controlName: string, errorCode: string): boolean {
    const control = this.form.get(controlName);
    return !!control && control.touched && control.hasError(errorCode);
  }

  selectImage(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    const allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];

    if (file && !allowedTypes.includes(file.type)) {
      this.selectedImage = null;
      this.imageError = 'Only JPEG, PNG and WebP images are supported.';
      input.value = '';
      return;
    }

    this.selectedImage = file;
    this.imageError = null;
  }
}

function futureDateValidator(control: AbstractControl<string>): ValidationErrors | null {
  if (!control.value) {
    return null;
  }

  const date = new Date(control.value);
  return Number.isNaN(date.getTime()) || date.getTime() <= Date.now() ? { futureDate: true } : null;
}

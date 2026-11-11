import { Component, inject, input, OnInit, output } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { TrainingCreatePayload, Training } from '../../training.model';
import { LocationMap } from '../../../../shared/components/location-map/location-map';
import { Modal } from '../../../../shared/components/modal/modal';
import { AuthService } from '../../../auth/auth.service';
import { SchoolService } from '../../../schools/school.service';
import { School } from '../../../schools/school.model';

export type TrainingFormValue = TrainingCreatePayload;

export interface TrainingFormSubmission {
  training: TrainingFormValue;
  image: File | null;
}

@Component({
  selector: 'app-training-form',
  imports: [ReactiveFormsModule, LocationMap, Modal],
  templateUrl: './training-form.html',
})
export class TrainingForm implements OnInit {
  trainingSubmitted = output<TrainingFormSubmission>();
  cancelled = output<void>();
  training = input<Training | null>(null);
  schoolId = input<number | null>(null);

  private readonly fb = new FormBuilder();
  private readonly auth = inject(AuthService);
  private readonly schoolService = inject(SchoolService);
  selectedImage: File | null = null;
  imageError: string | null = null;
  schools: School[] = [];
  showMap = false;
  confirmNotification = false;

  form = this.fb.nonNullable.group({
    notifySchool: [true],
    locationAddress: ['', Validators.maxLength(500)],
    latitude: [null as number | null],
    longitude: [null as number | null],
    schoolId: [0, [Validators.required, Validators.min(1)]],
    title: [
      '',
      [
        Validators.required,
        Validators.pattern(/\S/),
        Validators.minLength(3),
        Validators.maxLength(200),
      ],
    ],
    trainerName: [
      '',
      [
        Validators.required,
        Validators.pattern(/\S/),
        Validators.minLength(2),
        Validators.maxLength(200),
      ],
    ],
    startAt: ['', [Validators.required, futureDateValidator]],
    capacity: [null as number | null, [Validators.min(1), Validators.pattern(/^\d+$/)]],
    description: ['', Validators.maxLength(2000)],
    eventKind: ['TRAINING' as const],
    priceMode: ['TO_BE_AGREED' as 'PAID' | 'FREE' | 'TO_BE_AGREED'],
    price: [null as number | null],
  });

  ngOnInit(): void {
    this.schoolService.mine$().subscribe({
      next: (schools) => {
        this.schools = schools;
        const fixedSchoolId = this.schoolId();
        if (!this.training() && fixedSchoolId !== null) {
          this.form.controls.schoolId.setValue(fixedSchoolId);
        } else if (!this.training() && schools.length === 1) {
          this.form.controls.schoolId.setValue(schools[0].id);
        }
      },
    });
    const training = this.training();
    if (training) {
      this.form.patchValue({
        notifySchool: false,
        schoolId: training.schoolId,
        title: training.title,
        trainerName: training.trainerName,
        startAt: training.startAt.slice(0, 16),
        capacity: training.capacity,
        description: training.description ?? '',
        priceMode: training.priceMode ?? 'TO_BE_AGREED',
        price: training.price ?? null,
        locationAddress: training.locationAddress ?? '',
        latitude: training.latitude ?? null,
        longitude: training.longitude ?? null,
      });
    } else {
      this.form.controls.trainerName.setValue(this.auth.currentUser()?.displayName ?? '');
      if (this.schoolId() !== null) this.form.controls.schoolId.setValue(this.schoolId()!);
    }
    this.priceModeChanged();
  }

  priceModeChanged(): void {
    const price = this.form.controls.price;
    if (this.form.controls.priceMode.value === 'PAID') {
      price.setValidators([
        Validators.required,
        Validators.min(0.01),
        Validators.max(99999999.99),
        Validators.pattern(/^\d+(\.\d{1,2})?$/),
      ]);
    } else {
      price.clearValidators();
      price.setValue(null);
    }
    price.updateValueAndValidity();
  }

  get editing(): boolean {
    return this.training() !== null;
  }

  submit(): void {
    if (this.form.invalid || this.imageError) {
      this.form.markAllAsTouched();
      return;
    }

    if (!this.editing && this.form.controls.notifySchool.value) {
      this.confirmNotification = true;
      return;
    }
    this.emitTraining();
  }

  emitTraining(): void {
    this.confirmNotification = false;
    this.trainingSubmitted.emit({
      training: this.form.getRawValue(),
      image: this.selectedImage,
    });
  }

  selectLocation(point: { latitude: number; longitude: number }): void {
    this.form.patchValue(point);
  }

  clearLocation(): void {
    this.form.patchValue({ latitude: null, longitude: null });
    this.showMap = false;
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
    if (!file) return;
    const allowedTypes = ['image/jpeg', 'image/png'];

    if (file && !allowedTypes.includes(file.type)) {
      this.selectedImage = null;
      this.imageError = 'Dozwolone są tylko zdjęcia JPG i PNG.';
      input.value = '';
      return;
    }

    if (file && file.size > 10 * 1024 * 1024) {
      this.selectedImage = null;
      this.imageError = 'Zdjęcie może mieć maksymalnie 10 MB.';
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

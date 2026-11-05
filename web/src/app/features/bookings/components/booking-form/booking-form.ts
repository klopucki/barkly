import { Component, output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

export interface BookingFormValue {
  ownerName: string;
  email: string;
  dogName: string;
  notes: string;
}

@Component({
  selector: 'app-booking-form',
  imports: [ReactiveFormsModule],
  templateUrl: './booking-form.html',
})
export class BookingForm {
  bookingSubmitted = output<BookingFormValue>();
  cancelled = output<void>();

  private readonly fb = new FormBuilder();

  bookingForm = this.fb.nonNullable.group({
    ownerName: [
      '',
      [
        Validators.required,
        Validators.pattern(/\S/),
        Validators.minLength(2),
        Validators.maxLength(200),
      ],
    ],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(320)]],
    dogName: [
      '',
      [
        Validators.required,
        Validators.pattern(/\S/),
        Validators.minLength(2),
        Validators.maxLength(200),
      ],
    ],
    notes: ['', [Validators.maxLength(1000)]],
  });

  submitBooking(): void {
    if (this.bookingForm.invalid) {
      this.bookingForm.markAllAsTouched();
      return;
    }

    this.bookingSubmitted.emit(this.bookingForm.getRawValue());

    this.bookingForm.reset({
      ownerName: '',
      email: '',
      dogName: '',
      notes: '',
    });
  }

  hasError(controlName: string, errorCode: string): boolean {
    const control = this.bookingForm.get(controlName);
    return !!control && control.touched && control.hasError(errorCode);
  }

  cancel(): void {
    this.cancelled.emit();
  }
}

import { Component, output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

export interface BookingFormValue {
  ownerName: string;
  email: string;
  dogName: string;
  dogAge: number;
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
    ownerName: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    dogName: ['', [Validators.required]],
    dogAge: [1, [Validators.required, Validators.min(0)]],
    notes: [''],
  });

  submitBooking(): void {
    console.log("xd");
    if (this.bookingForm.invalid) {
      this.bookingForm.markAllAsTouched();
      console.log("asda");
      return;
    }

    this.bookingSubmitted.emit(this.bookingForm.getRawValue());

    this.bookingForm.reset({
      ownerName: '',
      email: '',
      dogName: '',
      dogAge: 1,
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

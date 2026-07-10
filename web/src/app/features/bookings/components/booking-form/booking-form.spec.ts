import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BookingForm } from './booking-form';

describe('BookingForm', () => {
  let component: BookingForm;
  let fixture: ComponentFixture<BookingForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BookingForm],
    }).compileComponents();

    fixture = TestBed.createComponent(BookingForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('does not submit an invalid booking', () => {
    const emit = vi.spyOn(component.bookingSubmitted, 'emit');

    component.bookingForm.setValue({
      ownerName: 'A',
      email: 'invalid',
      dogName: '',
      dogAge: 31,
      notes: '',
    });
    component.submitBooking();

    expect(emit).not.toHaveBeenCalled();
    expect(component.bookingForm.touched).toBe(true);
  });

  it('submits a valid booking', () => {
    const emit = vi.spyOn(component.bookingSubmitted, 'emit');
    const booking = {
      ownerName: 'Jan Kowalski',
      email: 'jan@example.com',
      dogName: 'Burek',
      dogAge: 4,
      notes: '',
    };

    component.bookingForm.setValue(booking);
    component.submitBooking();

    expect(emit).toHaveBeenCalledWith(booking);
  });
});

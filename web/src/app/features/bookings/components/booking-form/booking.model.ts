export interface Booking {
  id: number;
  trainingId: number;
  dogId: number | null;
  ownerName: string;
  email: string;
  dogName: string;
  dogAge: number;
  notes: string;
  createdAt: string;
}

export interface MyTrainingBooking {
  bookingId: number;
  trainingId: number;
  dogId: number;
  dogName: string;
  trainingTitle: string;
  trainerName: string;
  trainingType: string;
  startAt: string;
}

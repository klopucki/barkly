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

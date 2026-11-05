export interface TrainingCreatePayload {
  notifySchool?: boolean;
  description?: string;
  eventKind?: 'TRAINING';
  priceMode?: 'PAID' | 'FREE' | 'TO_BE_AGREED';
  price?: number | null;
  locationAddress?: string | null;
  latitude?: number | null;
  longitude?: number | null;
  schoolId: number;
  title: string;
  trainerName: string;
  startAt: string;
  capacity: number | null;
}

export interface Training {
  id: number;
  description?: string;
  eventKind?: 'TRAINING';
  priceMode?: 'PAID' | 'FREE' | 'TO_BE_AGREED';
  price?: number | null;
  locationAddress?: string | null;
  latitude?: number | null;
  longitude?: number | null;
  schoolId: number;
  title: string;
  trainerName: string;
  startAt: string;
  capacity: number | null;
  bookedCount: number;
  imageKey: string | null;
  pawCount: number;
  pawedByCurrentUser: boolean;
}

export function trainingImageUrl(imageKey: string): string {
  return `/api/training-images/${encodeURIComponent(imageKey)}`;
}

export function trainingPriceLabel(training: Training): string {
  if (training.priceMode === 'FREE') return 'Bezpłatnie';
  if (training.priceMode === 'PAID' && training.price != null) {
    return (
      new Intl.NumberFormat('pl-PL', { style: 'currency', currency: 'PLN' }).format(
        training.price,
      ) + ' / pies'
    );
  }
  return 'Cena do ustalenia';
}

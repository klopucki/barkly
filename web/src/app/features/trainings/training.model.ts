export type TrainingLevel = 'puppy' | 'basic' | 'advanced' | 'sport';

export interface Training {
  id: number;
  schoolId: number;
  title: string;
  trainerName: string;
  level: TrainingLevel;
  startAt: string;
  capacity: number | null;
  bookedCount: number;
}

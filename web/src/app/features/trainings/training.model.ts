export type TrainingLevel = 'PUPPY' | 'BASIC' | 'ADVANCED' | 'SPORT';

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

export interface DictionaryValue {
  id: number;
  code: string;
  name: string;
}

export interface TrainingLevelDictionary extends DictionaryValue {
  trainingTypeId: number | null;
}

export interface TrainingDictionaries {
  trainingTypes: DictionaryValue[];
  trainingLevels: TrainingLevelDictionary[];
  targetGroups: DictionaryValue[];
}

export interface TrainingCreatePayload {
  schoolId: number;
  title: string;
  trainerName: string;
  trainingTypeId: number;
  trainingLevelId: number | null;
  targetGroupId: number | null;
  homeVisit: boolean;
  startAt: string;
  capacity: number | null;
}

export interface Training {
  id: number;
  schoolId: number;
  title: string;
  trainerName: string;
  trainingType: DictionaryValue;
  trainingLevel: DictionaryValue | null;
  targetGroup: DictionaryValue | null;
  homeVisit: boolean;
  startAt: string;
  capacity: number | null;
  bookedCount: number;
  imageKey: string | null;
}

export function trainingImageUrl(imageKey: string): string {
  return `/api/training-images/${encodeURIComponent(imageKey)}`;
}

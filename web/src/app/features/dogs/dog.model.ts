export type DogVisibility = 'PRIVATE' | 'MEMBERS' | 'PUBLIC';

export interface DogImage {
  id: number;
  visibility: DogVisibility;
}

export interface Dog {
  id: number;
  name: string;
  breed: string | null;
  birthDate: string | null;
  sex: string | null;
  description: string;
  visibility: DogVisibility;
  owner: boolean;
  images: DogImage[];
}

export interface DogPayload {
  name: string;
  breed: string | null;
  birthDate: string | null;
  sex: string | null;
  description: string;
  visibility: DogVisibility;
}

export const dogImageUrl = (id: number) => `/api/dog-images/${id}`;

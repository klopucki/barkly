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

export interface DogPost {
  id: number;
  dogId: number;
  dogName: string;
  ownerDisplayName: string;
  content: string;
  hasImage: boolean;
  publishedAt: string;
  reactions: PostReactionSummary[];
  myReaction: PostReactionType | null;
  commentCount: number;
}

export type PostReactionType = 'HEART' | 'THUMBS_UP' | 'PAW';

export interface PostReactionSummary {
  reactionType: PostReactionType;
  count: number;
}

export interface DogPostComment {
  id: number;
  authorDisplayName: string;
  content: string;
  publishedAt: string;
  mine: boolean;
}

export const dogImageUrl = (id: number) => `/api/dog-images/${id}`;
export const dogPostImageUrl = (id: number) => `/api/dog-post-images/${id}`;

export interface SchoolImage { id: number; imageKey: string; }
export interface School { id: number; name: string; slug: string; address: string; krs: string | null; description: string; activities: string; pricing: string; images: SchoolImage[]; }
export interface SchoolPayload { name: string; address: string; krs: string | null; description: string; activities: string; pricing: string; }
export interface SchoolNews { id: number; schoolId: number; schoolName: string; authorId: number; authorDisplayName: string; title: string; content: string; active: boolean; publishedAt: string; }
export const schoolImageUrl = (key: string) => `/api/training-images/${encodeURIComponent(key)}`;

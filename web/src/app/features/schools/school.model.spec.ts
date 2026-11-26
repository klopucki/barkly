import { School, schoolProfileImageKey } from './school.model';

describe('school profile photo', () => {
  it('preserves the existing first image until a profile image is selected', () => {
    const school = { images: [{ id: 1, imageKey: 'existing.jpg' }] } as School;
    expect(schoolProfileImageKey(school)).toBe('existing.jpg');
    expect(schoolProfileImageKey({ ...school, profileImageKey: 'new.jpg' })).toBe('new.jpg');
    expect(schoolProfileImageKey({ ...school, images: [] })).toBeNull();
  });
});

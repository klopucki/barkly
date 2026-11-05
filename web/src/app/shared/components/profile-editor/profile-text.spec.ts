import { renderProfileText } from './profile-text.pipe';
import { formattedProfile, unsafeProfile } from './testing/profile-text-fixtures';
import { displaysFormatting, escapesHtml } from './testing/profile-text-assertions';

describe('profile text', () => {
  it('renders the formatting used by the editor', () => {
    const result = renderProfileText(formattedProfile);
    displaysFormatting(result);
  });

  it('treats HTML as text', () => {
    const result = renderProfileText(unsafeProfile);
    escapesHtml(result);
  });
});

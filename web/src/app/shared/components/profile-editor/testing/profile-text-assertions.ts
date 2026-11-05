import { expect } from 'vitest';
export function displaysFormatting(result: string): void {
  expect(result).toContain('<strong>Nasza szkoła</strong>');
  expect(result).toContain('<strong>Blisko</strong>');
  expect(result).toContain('<em>spokojnie</em>');
  expect(result).toContain('• Trening');
}
export function escapesHtml(result: string): void {
  expect(result).not.toContain('<img');
  expect(result).not.toContain('<script>');
  expect(result).toContain('&lt;img');
  expect(result).toContain('&lt;script&gt;');
}

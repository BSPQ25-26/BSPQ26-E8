// @ts-check
const { test, expect } = require('@playwright/test');
const path = require('path');

const problemsPage = `file://${path.resolve(__dirname, '..', 'problems.html')}`;

const DATASET = [
  { slug: 'anagram-check', title: 'Anagram Check', difficulty: 'EASY', createdAt: '2026-01-04T10:00:00Z', languages: ['Python 3'], languageCodes: ['python'] },
  { slug: 'two-sum', title: 'Two Sum', difficulty: 'EASY', createdAt: '2026-01-01T10:00:00Z', languages: ['Python 3', 'Node.js'], languageCodes: ['python', 'javascript'] },
  { slug: 'coin-change', title: 'Coin Change', difficulty: 'MEDIUM', createdAt: '2026-01-02T10:00:00Z', languages: ['Java'], languageCodes: ['java'] },
  { slug: 'grid-path', title: 'Grid Path', difficulty: 'HARD', createdAt: '2026-01-03T10:00:00Z', languages: ['C++'], languageCodes: ['cpp'] },
];

async function mockProblemsApi(page) {
  await page.route('http://localhost:10000/api/problems**', async (route) => {
    const url = new URL(route.request().url());
    const difficulty = (url.searchParams.get('difficulty') || '').toUpperCase();
    const name = (url.searchParams.get('name') || '').toLowerCase();
    const language = (url.searchParams.get('language') || '').toLowerCase();

    const filtered = DATASET.filter((problem) => {
      const matchesDifficulty = !difficulty || problem.difficulty === difficulty;
      const matchesName = !name || problem.title.toLowerCase().includes(name);
      const matchesLanguage = !language || problem.languageCodes.includes(language);
      return matchesDifficulty && matchesName && matchesLanguage;
    });

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(filtered),
    });
  });
}

test.describe('Problems page filtering', () => {
  test('renders dataset cards and count', async ({ page }) => {
    await mockProblemsApi(page);
    await page.goto(problemsPage);

    await expect(page.locator('.problem-card')).toHaveCount(4);
    await expect(page.locator('#problems-count')).toHaveText('4 problems found');
    await expect(page.locator('.problem-card-title').first()).toHaveText('Anagram Check');
    await expect(page.locator('.problem-card-language').first()).toHaveText('Language: Python 3');
  });

  test('filters by name', async ({ page }) => {
    await mockProblemsApi(page);
    await page.goto(problemsPage);

    await page.fill('#search-name', 'coin');
    await page.waitForTimeout(350);

    await expect(page.locator('.problem-card')).toHaveCount(1);
    await expect(page.locator('.problem-card-title')).toHaveText('Coin Change');
    await expect(page.locator('#problems-count')).toHaveText('1 problems found');
  });

  test('filters by difficulty and language, then resets', async ({ page }) => {
    await mockProblemsApi(page);
    await page.goto(problemsPage);

    await page.selectOption('#filter-difficulty', 'HARD');
    await expect(page.locator('.problem-card')).toHaveCount(1);
    await expect(page.locator('.problem-card-title')).toHaveText('Grid Path');

    await page.selectOption('#filter-language', 'java');
    await expect(page.locator('.problem-card')).toHaveCount(0);

    await page.click('#clear-filters');
    await expect(page.locator('.problem-card')).toHaveCount(4);
    await expect(page.locator('#problems-count')).toHaveText('4 problems found');
  });

  test('applies sorting options', async ({ page }) => {
    await mockProblemsApi(page);
    await page.goto(problemsPage);

    await page.selectOption('#filter-sort', 'alphabetical');
    await expect(page.locator('.problem-card-title').first()).toHaveText('Anagram Check');

    await page.selectOption('#filter-sort', 'created_at');
    await expect(page.locator('.problem-card-title').first()).toHaveText('Anagram Check');
  });
});

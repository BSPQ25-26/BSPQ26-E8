// @ts-check
//
// Acceptance test — Problem creation flow
// ----------------------------------------
// Main scenario: create a problem on create.html, then verify it appears on
// problems.html. A single in-memory mock backend persists state across both
// page navigations via closure (see mockBackend).
//
// Reuses the project's existing Playwright setup (playwright.config.js):
//   - file:// baseURL serving the static frontend
//   - Same auth bypass pattern as responsiveness.spec.js (fake token in localStorage)
//   - Same route-mocking pattern as problems-page.spec.js
//
// These are functional tests, so they only run on the desktop project — the
// mobile/tablet viewports are exercised by responsiveness.spec.js.

const { test, expect } = require('@playwright/test');
const path = require('path');

const pages = {
  create:   `file://${path.resolve(__dirname, '..', 'create.html')}`,
  problems: `file://${path.resolve(__dirname, '..', 'problems.html')}`,
};

// create.js / problems.js redirect to login when no token is present.
// auth.isAuthenticated() only checks that the key exists, not that it's valid.
async function setupAuth(page) {
  await page.addInitScript(() => {
    localStorage.setItem('session_token', 'fake-test-token');
  });
}

/**
 * Stand-in backend that lives in test memory and survives page navigation.
 * Handles:
 *   POST /api/problems        → appends and returns 201 with the created record
 *   GET  /api/problems        → returns the current state, filtered by query params
 *   GET  /api/problems/<id>   → returns the full record (used by the detail panel)
 */
async function mockBackend(page, initialProblems = []) {
  const state = { problems: [...initialProblems] };

  await page.route('http://localhost:10000/api/problems**', async (route) => {
    const request  = route.request();
    const method   = request.method();
    const url      = new URL(request.url());
    const pathname = url.pathname;

    // Detail endpoint: /api/problems/<id>
    const detailMatch = pathname.match(/^\/api\/problems\/([^/]+)$/);
    if (method === 'GET' && detailMatch) {
      const id = detailMatch[1];
      const problem = state.problems.find((p) => p.id === id);
      if (!problem) {
        await route.fulfill({ status: 404, contentType: 'application/json', body: '{}' });
        return;
      }
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(problem),
      });
      return;
    }

    if (method === 'POST') {
      const body = JSON.parse(request.postData() || '{}');
      const slug = body.slug || (body.title || '').toLowerCase().trim().replace(/\s+/g, '-');
      // Preserve every field submitted — problems.js will read them back from the detail endpoint.
      const created = {
        ...body,
        id:            `id-${state.problems.length + 1}`,
        slug,
        createdAt:     new Date().toISOString(),
        languages:     ['Python 3'],
        languageCodes: ['python'],
      };
      state.problems.push(created);

      await route.fulfill({
        status: 201,
        contentType: 'application/json',
        body: JSON.stringify(created),
      });
      return;
    }

    if (method === 'GET') {
      const difficulty = (url.searchParams.get('difficulty') || '').toUpperCase();
      const name       = (url.searchParams.get('name')       || '').toLowerCase();
      const language   = (url.searchParams.get('language')   || '').toLowerCase();

      const filtered = state.problems.filter((p) => {
        const okDifficulty = !difficulty || p.difficulty === difficulty;
        const okName       = !name       || (p.title || '').toLowerCase().includes(name);
        const okLanguage   = !language   || (p.languageCodes || []).includes(language);
        return okDifficulty && okName && okLanguage;
      });

      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(filtered),
      });
      return;
    }

    await route.continue();
  });
}

/**
 * Fills the required form fields and submits, awaiting the success banner.
 * @param {import('@playwright/test').Page} page
 * @param {{ title: string, statementMd: string, difficulty: 'EASY' | 'MEDIUM' | 'HARD', constraintsMd?: string }} fields
 */
async function createProblem(page, { title, statementMd, difficulty, constraintsMd }) {
  await page.fill('#title', title);
  await page.fill('#statementMd', statementMd);
  if (constraintsMd) await page.fill('#constraintsMd', constraintsMd);
  await page.selectOption('#difficulty', difficulty);
  await page.click('#submit-btn');
  await expect(page.locator('#status-message')).toHaveAttribute('data-status', 'success');
}

test.describe('Problem creation flow', () => {
  // Functional test — run once, on desktop only.
  test.beforeEach(async ({}, testInfo) => {
    test.skip(testInfo.project.name !== 'desktop', 'Functional test runs on desktop only');
  });

  test('creates a new problem and shows it on the problems list', async ({ page }) => {
    await setupAuth(page);
    await mockBackend(page, [
      {
        slug:          'two-sum',
        title:         'Two Sum',
        difficulty:    'EASY',
        createdAt:     '2026-01-01T10:00:00Z',
        languages:     ['Python 3'],
        languageCodes: ['python'],
      },
    ]);

    // ── Step 1: fill the form on create.html ──────────────────────────────
    await page.goto(pages.create);

    await page.fill('#title',         'Reverse Linked List');
    await page.fill('#statementMd',   'Given the head of a singly linked list, reverse the list and return its head.');
    await page.fill('#constraintsMd', '0 <= n <= 5000');
    await page.selectOption('#difficulty', 'MEDIUM');
    await page.click('#submit-btn');

    // Status banner reflects the success returned by the mock POST
    const status = page.locator('#status-message');
    await expect(status).toHaveText('Problem "Reverse Linked List" created successfully!');
    await expect(status).toHaveAttribute('data-status', 'success');

    // create.js calls form.reset() after success — fields should be empty again
    await expect(page.locator('#title')).toHaveValue('');
    await expect(page.locator('#statementMd')).toHaveValue('');

    // ── Step 2: confirm it shows up on problems.html ──────────────────────
    await page.goto(pages.problems);

    await expect(page.locator('.problem-card')).toHaveCount(2);

    const titles = page.locator('.problem-card-title');
    await expect(titles).toContainText(['Two Sum', 'Reverse Linked List']);

    // Filtering by name should isolate the new problem
    await page.fill('#search-name', 'reverse');
    await expect(page.locator('.problem-card')).toHaveCount(1);
    await expect(page.locator('.problem-card-title')).toHaveText('Reverse Linked List');
  });

  test('blocks submission when required fields are missing', async ({ page }) => {
    await setupAuth(page);

    // Track whether create.js actually fires a POST despite the empty form
    let postCount = 0;
    await page.route('http://localhost:10000/api/problems**', async (route) => {
      if (route.request().method() === 'POST') postCount += 1;
      await route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
    });

    await page.goto(pages.create);
    await page.click('#submit-btn');

    const status = page.locator('#status-message');
    await expect(status).toHaveText('Title, statement and difficulty are required.');
    await expect(status).toHaveAttribute('data-status', 'error');
    expect(postCount).toBe(0);
  });

  test('shows a conflict message when the slug is already taken', async ({ page }) => {
    await setupAuth(page);
    await page.route('http://localhost:10000/api/problems**', async (route) => {
      if (route.request().method() === 'POST') {
        await route.fulfill({
          status: 409,
          contentType: 'application/json',
          body: JSON.stringify({ error: 'A problem with that slug already exists.' }),
        });
        return;
      }
      await route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
    });

    await page.goto(pages.create);
    await page.fill('#title',       'Two Sum');
    await page.fill('#statementMd', 'Duplicate of an existing problem.');
    await page.selectOption('#difficulty', 'EASY');
    await page.click('#submit-btn');

    const status = page.locator('#status-message');
    await expect(status).toContainText('Conflict:');
    await expect(status).toHaveAttribute('data-status', 'error');
  });

  test('shows the created problem details when its card is clicked', async ({ page }) => {
    await setupAuth(page);
    await mockBackend(page);

    await page.goto(pages.create);
    await createProblem(page, {
      title:         'Binary Search',
      statementMd:   'Given a sorted array and a target, return the target index or -1.',
      constraintsMd: '1 <= n <= 10^5 and array is sorted ascending',
      difficulty:    'EASY',
    });

    await page.goto(pages.problems);

    const card = page.locator('.problem-card').first();
    await expect(card).toBeVisible();
    await card.click();

    // Detail panel expands and renders the statement + constraints sections
    await expect(card).toHaveClass(/is-expanded/);
    const detail = card.locator('.problem-detail');
    await expect(detail).toHaveClass(/is-open/);
    await expect(detail).toContainText('Given a sorted array and a target');
    await expect(detail).toContainText('1 <= n <= 10^5 and array is sorted ascending');
    await expect(detail.locator('.problem-detail-start-btn')).toBeVisible();

    // Clicking again collapses the detail panel
    await card.click();
    await expect(card).not.toHaveClass(/is-expanded/);
    await expect(detail).not.toHaveClass(/is-open/);
  });

  test('renders the correct badge and card colour for each difficulty', async ({ page }) => {
    await setupAuth(page);
    await mockBackend(page);

    await page.goto(pages.create);
    await createProblem(page, { title: 'Easy One',   statementMd: 'Trivial problem.',     difficulty: 'EASY'   });
    await createProblem(page, { title: 'Medium One', statementMd: 'Moderate problem.',    difficulty: 'MEDIUM' });
    await createProblem(page, { title: 'Hard One',   statementMd: 'Difficult problem.',   difficulty: 'HARD'   });

    await page.goto(pages.problems);
    await expect(page.locator('.problem-card')).toHaveCount(3);

    // Default sort is by difficulty (EASY → MEDIUM → HARD), then alphabetical
    const cards   = page.locator('.problem-card');
    const titles  = cards.locator('.problem-card-title');
    const badges  = cards.locator('.badge');
    await expect(titles).toHaveText(['Easy One', 'Medium One', 'Hard One']);

    await expect(cards.nth(0)).toHaveClass(/card--green/);
    await expect(cards.nth(1)).toHaveClass(/card--orange/);
    await expect(cards.nth(2)).toHaveClass(/card--red/);

    await expect(badges.nth(0)).toHaveClass(/badge--easy/);
    await expect(badges.nth(1)).toHaveClass(/badge--medium/);
    await expect(badges.nth(2)).toHaveClass(/badge--hard/);
  });

  test('sends the exact field values typed into the form to the API', async ({ page }) => {
    await setupAuth(page);

    /** @type {any} */
    let capturedBody = null;
    await page.route('http://localhost:10000/api/problems**', async (route) => {
      const request = route.request();
      if (request.method() === 'POST') {
        capturedBody = JSON.parse(request.postData() || '{}');
        await route.fulfill({
          status: 201,
          contentType: 'application/json',
          body: JSON.stringify({ ...capturedBody, id: 'id-1', createdAt: '2026-01-01T00:00:00Z' }),
        });
        return;
      }
      await route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
    });

    await page.goto(pages.create);
    await page.fill('#title',            'Two Sum');
    await page.fill('#statementMd',      'Given an array of integers, return indices of the two numbers that add up to a target.');
    await page.fill('#constraintsMd',    '2 <= nums.length <= 10^4');
    await page.fill('#solutionTemplate', 'def two_sum(nums, target):\n    pass');
    await page.selectOption('#difficulty', 'EASY');
    await page.click('#submit-btn');

    await expect(page.locator('#status-message')).toHaveAttribute('data-status', 'success');

    expect(capturedBody).not.toBeNull();
    expect(capturedBody.title).toBe('Two Sum');
    expect(capturedBody.statementMd).toBe('Given an array of integers, return indices of the two numbers that add up to a target.');
    expect(capturedBody.constraintsMd).toBe('2 <= nums.length <= 10^4');
    expect(capturedBody.solutionTemplate).toBe('def two_sum(nums, target):\n    pass');
    expect(capturedBody.difficulty).toBe('EASY');

    // Fields that have no input element in create.html are submitted as null
    expect(capturedBody.slug).toBeNull();
    expect(capturedBody.inputSpecMd).toBeNull();
    expect(capturedBody.outputSpecMd).toBeNull();
    expect(capturedBody.hintsMd).toBeNull();
    expect(capturedBody.languageCompilationConfig).toBeNull();
  });

  test('card and detail panel display the same values the user submitted', async ({ page }) => {
    await setupAuth(page);
    await mockBackend(page);

    const submitted = {
      title:         'Longest Common Subsequence',
      statementMd:   'Given two strings text1 and text2, return the length of their longest common subsequence.',
      constraintsMd: '1 <= text1.length, text2.length <= 1000',
      /** @type {'MEDIUM'} */
      difficulty:    'MEDIUM',
    };

    await page.goto(pages.create);
    await createProblem(page, submitted);

    await page.goto(pages.problems);

    // ── Card surface reflects title, language, and difficulty styling ─────
    const card = page.locator('.problem-card');
    await expect(card).toHaveCount(1);
    await expect(card.locator('.problem-card-title')).toHaveText(submitted.title);
    await expect(card.locator('.problem-card-language')).toHaveText('Language: Python 3');
    await expect(card.locator('.badge')).toHaveClass(/badge--medium/);
    await expect(card).toHaveClass(/card--orange/);

    // ── Detail surface reflects the markdown body fields ──────────────────
    await card.click();
    const detail = card.locator('.problem-detail');
    await expect(detail).toHaveClass(/is-open/);
    await expect(detail).toContainText(submitted.statementMd);
    await expect(detail).toContainText(submitted.constraintsMd);

    // Sections that were left blank on the form are not rendered
    const sections = detail.locator('.problem-detail-section-title');
    const sectionTitles = await sections.allTextContents();
    expect(sectionTitles.some((t) => /input/i.test(t))).toBe(false);
    expect(sectionTitles.some((t) => /output/i.test(t))).toBe(false);
    expect(sectionTitles.some((t) => /hint/i.test(t))).toBe(false);
  });

  test('difficulty filter isolates problems created with that difficulty', async ({ page }) => {
    await setupAuth(page);
    await mockBackend(page);

    await page.goto(pages.create);
    await createProblem(page, { title: 'Alpha', statementMd: 'A simple one.',  difficulty: 'EASY' });
    await createProblem(page, { title: 'Beta',  statementMd: 'A tricky one.',  difficulty: 'HARD' });
    await createProblem(page, { title: 'Gamma', statementMd: 'Another hard.',  difficulty: 'HARD' });

    await page.goto(pages.problems);
    await expect(page.locator('.problem-card')).toHaveCount(3);

    await page.selectOption('#filter-difficulty', 'HARD');
    await expect(page.locator('.problem-card')).toHaveCount(2);
    await expect(page.locator('.problem-card-title')).toContainText(['Beta', 'Gamma']);

    await page.selectOption('#filter-difficulty', 'EASY');
    await expect(page.locator('.problem-card')).toHaveCount(1);
    await expect(page.locator('.problem-card-title')).toHaveText('Alpha');

    await page.click('#clear-filters');
    await expect(page.locator('.problem-card')).toHaveCount(3);
  });
});

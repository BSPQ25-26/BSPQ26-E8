// @ts-check
//
// Acceptance test — Problem edit & delete (CRUD U + D)
// -----------------------------------------------------
// Edit flow: edit.html?id=<id> pre-fills the form from GET /api/problems/<id>
// and submits a PUT /api/problems/<id> on save.
// Delete flow: profile_page.html lists "My Problems" with Delete buttons that
// confirm() then DELETE /api/problems/<id>.
//
// Reuses the existing Playwright setup (file:// baseURL, fake-token auth bypass,
// route-mocking pattern from create-problem.spec.js / problems-page.spec.js).
// Functional coverage only — desktop project, mobile/tablet are out of scope.

const { test, expect } = require('@playwright/test');
const path = require('path');

const pages = {
  edit:    `file://${path.resolve(__dirname, '..', 'edit.html')}`,
  profile: `file://${path.resolve(__dirname, '..', 'profile_page.html')}`,
};

// auth.isAuthenticated() only checks that a token exists, and getValidToken()
// returns the token unchanged unless it has the special expirable payload format
// — a plain string like 'fake-test-token' skips the refresh path entirely.
async function setupAuth(page) {
  await page.addInitScript(() => {
    localStorage.setItem('session_token', 'fake-test-token');
    localStorage.setItem('user_data', JSON.stringify({
      id: 'u-1',
      username: 'testuser',
      email: 'test@example.com',
    }));
  });
}

/**
 * Stand-in backend that lives in test memory and survives page navigation.
 * Handles every endpoint edit.html / profile_page.html may call:
 *   GET    /api/problems/<id>           detail used by edit.js loadProblem()
 *   GET    /api/problems/by-author/<id> "My Problems" list on profile_page
 *   GET    /api/problems                fallback list (profile difficulty stats)
 *   GET    /api/submissions/mine        profile stats
 *   GET    /api/users/...               profile user refresh
 *   PUT    /api/problems/<id>           edit submission
 *   DELETE /api/problems/<id>           profile delete
 */
async function mockBackend(page, initialProblems = [], userId = 'u-1') {
  const state = { problems: [...initialProblems] };

  await page.route('http://localhost:10000/api/**', async (route) => {
    const request  = route.request();
    const method   = request.method();
    const url      = new URL(request.url());
    const pathname = url.pathname;

    // ── Problems by author (profile page "My Problems" table) ──────────────
    const byAuthorMatch = pathname.match(/^\/api\/problems\/by-author\/([^/]+)$/);
    if (method === 'GET' && byAuthorMatch) {
      const author = byAuthorMatch[1];
      const filtered = state.problems.filter((p) => (p.authorId || userId) === author);
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(filtered),
      });
      return;
    }

    // ── Problem examples (edit.js issues PUT alongside the main update) ────
    const examplesMatch = pathname.match(/^\/api\/problems\/([^/]+)\/examples$/);
    if (method === 'PUT' && examplesMatch) {
      const id  = examplesMatch[1];
      const idx = state.problems.findIndex((p) => p.id === id);
      if (idx === -1) {
        await route.fulfill({ status: 404, contentType: 'application/json', body: '{}' });
        return;
      }
      const body = JSON.parse(request.postData() || '[]');
      state.problems[idx] = { ...state.problems[idx], examples: body };
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(state.problems[idx].examples),
      });
      return;
    }

    // ── Problem detail / update / delete ───────────────────────────────────
    const detailMatch = pathname.match(/^\/api\/problems\/([^/]+)$/);
    if (detailMatch) {
      const id = detailMatch[1];
      const idx = state.problems.findIndex((p) => p.id === id);

      if (method === 'GET') {
        if (idx === -1) {
          await route.fulfill({ status: 404, contentType: 'application/json', body: '{}' });
          return;
        }
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(state.problems[idx]),
        });
        return;
      }

      if (method === 'PUT') {
        if (idx === -1) {
          await route.fulfill({ status: 404, contentType: 'application/json', body: '{}' });
          return;
        }
        const body = JSON.parse(request.postData() || '{}');
        state.problems[idx] = { ...state.problems[idx], ...body };
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(state.problems[idx]),
        });
        return;
      }

      if (method === 'DELETE') {
        if (idx === -1) {
          await route.fulfill({ status: 404, contentType: 'application/json', body: '{}' });
          return;
        }
        state.problems.splice(idx, 1);
        await route.fulfill({ status: 204, body: '' });
        return;
      }
    }

    // ── Generic problems list (profile difficulty stats) ───────────────────
    if (method === 'GET' && pathname === '/api/problems') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(state.problems),
      });
      return;
    }

    // ── Submissions list (profile stats) ───────────────────────────────────
    if (method === 'GET' && pathname.startsWith('/api/submissions')) {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: '[]',
      });
      return;
    }

    // ── User lookups (profile may refresh user data) ───────────────────────
    if (method === 'GET' && pathname.startsWith('/api/users')) {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ id: userId, username: 'testuser', email: 'test@example.com' }),
      });
      return;
    }

    // Anything we forgot to mock should fail loudly rather than hit the network.
    await route.fulfill({ status: 404, contentType: 'application/json', body: '{}' });
  });

  return state;
}

const SAMPLE_PROBLEM = {
  id:               'id-1',
  authorId:         'u-1',
  slug:             'two-sum',
  title:            'Two Sum',
  statementMd:      'Given an array of integers, return indices of the two numbers that add up to a target.',
  difficulty:       'EASY',
  constraintsMd:    '2 <= nums.length <= 10^4',
  solutionTemplate: 'def two_sum(nums, target):\n    pass',
  createdAt:        '2026-01-01T10:00:00Z',
  languages:        ['Python 3'],
  languageCodes:    ['python'],
};

test.describe('Problem edit flow', () => {
  // Functional test — run once, on desktop only.
  test.beforeEach(async ({}, testInfo) => {
    test.skip(testInfo.project.name !== 'desktop', 'Functional test runs on desktop only');
  });

  test('pre-fills the form with the problem\'s existing data', async ({ page }) => {
    await setupAuth(page);
    await mockBackend(page, [SAMPLE_PROBLEM]);

    await page.goto(`${pages.edit}?id=${SAMPLE_PROBLEM.id}`);

    // loadProblem() is async — wait for setField() to run before asserting.
    await expect(page.locator('#title')).toHaveValue(SAMPLE_PROBLEM.title);
    await expect(page.locator('#statementMd')).toHaveValue(SAMPLE_PROBLEM.statementMd);
    await expect(page.locator('#difficulty')).toHaveValue(SAMPLE_PROBLEM.difficulty);
    await expect(page.locator('#constraintsMd')).toHaveValue(SAMPLE_PROBLEM.constraintsMd);
    await expect(page.locator('#solutionTemplate')).toHaveValue(SAMPLE_PROBLEM.solutionTemplate);
  });

  test('saves changes and shows the success banner', async ({ page }) => {
    await setupAuth(page);
    await mockBackend(page, [SAMPLE_PROBLEM]);

    await page.goto(`${pages.edit}?id=${SAMPLE_PROBLEM.id}`);
    await expect(page.locator('#title')).toHaveValue(SAMPLE_PROBLEM.title);

    await page.fill('#title', 'Two Sum (Revised)');
    await page.click('#submit-btn');

    const status = page.locator('#status-message');
    await expect(status).toHaveText('Problem updated successfully!');
    await expect(status).toHaveAttribute('data-status', 'success');
  });

  test('sends the exact edited values to the API', async ({ page }) => {
    await setupAuth(page);
    await mockBackend(page, [SAMPLE_PROBLEM]);

    /** @type {any} */
    let capturedBody = null;
    // Layer a targeted spy on top of the mockBackend route — Playwright runs
    // handlers most-recent first, so this captures PUTs while everything else
    // (including the initial GET) still falls through to the closure backend.
    await page.route(`http://localhost:10000/api/problems/${SAMPLE_PROBLEM.id}`, async (route) => {
      if (route.request().method() === 'PUT') {
        capturedBody = JSON.parse(route.request().postData() || '{}');
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ ...SAMPLE_PROBLEM, ...capturedBody }),
        });
        return;
      }
      await route.fallback();
    });

    await page.goto(`${pages.edit}?id=${SAMPLE_PROBLEM.id}`);
    await expect(page.locator('#title')).toHaveValue(SAMPLE_PROBLEM.title);

    await page.fill('#title',            'Reverse Linked List');
    await page.fill('#statementMd',      'Reverse a singly linked list and return the new head.');
    await page.fill('#constraintsMd',    '0 <= n <= 5000');
    await page.fill('#solutionTemplate', 'def reverse(head):\n    return head');
    await page.selectOption('#difficulty', 'MEDIUM');

    await page.click('#submit-btn');
    await expect(page.locator('#status-message')).toHaveAttribute('data-status', 'success');

    expect(capturedBody).not.toBeNull();
    expect(capturedBody.title).toBe('Reverse Linked List');
    expect(capturedBody.statementMd).toBe('Reverse a singly linked list and return the new head.');
    expect(capturedBody.constraintsMd).toBe('0 <= n <= 5000');
    expect(capturedBody.solutionTemplate).toBe('def reverse(head):\n    return head');
    expect(capturedBody.difficulty).toBe('MEDIUM');
  });

  test('blocks submission when required fields are missing', async ({ page }) => {
    await setupAuth(page);
    await mockBackend(page, [SAMPLE_PROBLEM]);

    // Spy on PUTs so we can assert nothing was sent.
    let putCount = 0;
    await page.route(`http://localhost:10000/api/problems/${SAMPLE_PROBLEM.id}`, async (route) => {
      if (route.request().method() === 'PUT') putCount += 1;
      await route.fallback();
    });

    await page.goto(`${pages.edit}?id=${SAMPLE_PROBLEM.id}`);
    await expect(page.locator('#title')).toHaveValue(SAMPLE_PROBLEM.title);

    await page.fill('#title', '');
    await page.click('#submit-btn');

    const status = page.locator('#status-message');
    await expect(status).toHaveText('Title, statement and difficulty are required.');
    await expect(status).toHaveAttribute('data-status', 'error');
    expect(putCount).toBe(0);
  });

  test('redirects to the profile page when the id query parameter is missing', async ({ page }) => {
    await setupAuth(page);
    await mockBackend(page);

    await page.goto(pages.edit);

    // edit.js redirects via window.location.href = 'profile_page.html'.
    await page.waitForURL(/profile_page\.html$/);
    expect(page.url()).toMatch(/profile_page\.html$/);
  });

  test('shows a load error when the problem cannot be fetched', async ({ page }) => {
    await setupAuth(page);
    await mockBackend(page, [SAMPLE_PROBLEM]);

    // Override the detail GET to fail. Most-recent route runs first, so this
    // intercepts before the mockBackend closure ever sees the request.
    await page.route(`http://localhost:10000/api/problems/${SAMPLE_PROBLEM.id}`, async (route) => {
      if (route.request().method() === 'GET') {
        await route.fulfill({
          status: 500,
          contentType: 'application/json',
          body: JSON.stringify({ error: 'Internal server error' }),
        });
        return;
      }
      await route.fallback();
    });

    await page.goto(`${pages.edit}?id=${SAMPLE_PROBLEM.id}`);

    const status = page.locator('#status-message');
    await expect(status).toHaveText('Could not load the problem. Please go back and try again.');
    await expect(status).toHaveAttribute('data-status', 'error');

    // Form stayed empty — nothing was pre-filled.
    await expect(page.locator('#title')).toHaveValue('');
    await expect(page.locator('#statementMd')).toHaveValue('');
  });

  test('shows a forbidden error when the user is not allowed to edit the problem', async ({ page }) => {
    await setupAuth(page);
    await mockBackend(page, [SAMPLE_PROBLEM]);

    // GET still serves the form; only the PUT is blocked.
    await page.route(`http://localhost:10000/api/problems/${SAMPLE_PROBLEM.id}`, async (route) => {
      if (route.request().method() === 'PUT') {
        await route.fulfill({
          status: 403,
          contentType: 'application/json',
          body: JSON.stringify({ error: 'Forbidden' }),
        });
        return;
      }
      await route.fallback();
    });

    await page.goto(`${pages.edit}?id=${SAMPLE_PROBLEM.id}`);
    await expect(page.locator('#title')).toHaveValue(SAMPLE_PROBLEM.title);

    await page.fill('#title', 'Two Sum (Revised)');
    await page.click('#submit-btn');

    const status = page.locator('#status-message');
    await expect(status).toHaveText('You are not allowed to edit this problem.');
    await expect(status).toHaveAttribute('data-status', 'error');

    // The submit button must recover after the failed request — the finally
    // block in submitEditProblem() re-enables it and restores the label.
    const submit = page.locator('#submit-btn');
    await expect(submit).toBeEnabled();
    await expect(submit).toHaveText('Save Changes');
  });

  test('shows a network error when the server cannot be reached', async ({ page }) => {
    await setupAuth(page);
    await mockBackend(page, [SAMPLE_PROBLEM]);

    // GET pre-fills the form; PUT is aborted to simulate a dropped connection.
    await page.route(`http://localhost:10000/api/problems/${SAMPLE_PROBLEM.id}`, async (route) => {
      if (route.request().method() === 'PUT') {
        await route.abort('failed');
        return;
      }
      await route.fallback();
    });

    await page.goto(`${pages.edit}?id=${SAMPLE_PROBLEM.id}`);
    await expect(page.locator('#title')).toHaveValue(SAMPLE_PROBLEM.title);

    await page.fill('#title', 'Two Sum (Revised)');
    await page.click('#submit-btn');

    const status = page.locator('#status-message');
    await expect(status).toHaveText('Could not reach the server. Is the backend running?');
    await expect(status).toHaveAttribute('data-status', 'error');
  });
});

const TWO_PROBLEMS = [
  { ...SAMPLE_PROBLEM, id: 'id-1', title: 'Two Sum',            difficulty: 'EASY'   },
  { ...SAMPLE_PROBLEM, id: 'id-2', title: 'Reverse Linked List', difficulty: 'MEDIUM' },
];

test.describe('Problem delete flow', () => {
  test.beforeEach(async ({}, testInfo) => {
    test.skip(testInfo.project.name !== 'desktop', 'Functional test runs on desktop only');
  });

  test('removes the row and decrements the count when the user confirms', async ({ page }) => {
    await setupAuth(page);
    await mockBackend(page, TWO_PROBLEMS);

    // Accept every dialog (one confirm() in this scenario).
    page.on('dialog', (dialog) => dialog.accept());

    await page.goto(pages.profile);

    // Wait for the My Problems table and the created-count to render.
    await expect(page.locator('[data-delete]')).toHaveCount(2);
    await expect(page.locator('#profile-created-count')).toHaveText('2');

    // Capture the DELETE so we can wait for it deterministically.
    const deleteResponse = page.waitForResponse(
      (resp) =>
        resp.url() === 'http://localhost:10000/api/problems/id-1' &&
        resp.request().method() === 'DELETE',
    );

    await page.click('[data-delete="id-1"]');
    const response = await deleteResponse;
    expect(response.status()).toBe(204);

    // The deleted row is gone, the surviving row stays, the count is decremented.
    await expect(page.locator('[data-delete]')).toHaveCount(1);
    await expect(page.locator('[data-delete="id-1"]')).toHaveCount(0);
    await expect(page.locator('[data-delete="id-2"]')).toHaveCount(1);
    await expect(page.locator('#profile-created-count')).toHaveText('1');
  });

  test('does not delete when the user cancels the confirmation dialog', async ({ page }) => {
    await setupAuth(page);
    await mockBackend(page, TWO_PROBLEMS);

    // Dismiss the confirm() → returns false → deleteProblem() bails out early.
    page.on('dialog', (dialog) => dialog.dismiss());

    // Spy on DELETEs to prove none fire.
    let deleteCount = 0;
    await page.route('http://localhost:10000/api/problems/**', async (route) => {
      if (route.request().method() === 'DELETE') deleteCount += 1;
      await route.fallback();
    });

    await page.goto(pages.profile);
    await expect(page.locator('[data-delete]')).toHaveCount(2);
    await expect(page.locator('#profile-created-count')).toHaveText('2');

    await page.click('[data-delete="id-1"]');

    // Give the click a moment to flush — if a DELETE were going to fire, it would
    // have done so synchronously after confirm() returned. State stays untouched.
    await page.waitForTimeout(100);
    expect(deleteCount).toBe(0);
    await expect(page.locator('[data-delete]')).toHaveCount(2);
    await expect(page.locator('#profile-created-count')).toHaveText('2');
  });

  test('shows an alert and keeps the row when the delete request fails', async ({ page }) => {
    await setupAuth(page);
    await mockBackend(page, TWO_PROBLEMS);

    // The confirm() fires first, then on 403 the catch block fires alert().
    // Accept both, capturing their messages so we can assert on the alert.
    /** @type {{type: string, message: string}[]} */
    const dialogs = [];
    page.on('dialog', async (dialog) => {
      dialogs.push({ type: dialog.type(), message: dialog.message() });
      await dialog.accept();
    });

    // Override DELETE for id-1 only — leave id-2 alone.
    await page.route('http://localhost:10000/api/problems/id-1', async (route) => {
      if (route.request().method() === 'DELETE') {
        await route.fulfill({
          status: 403,
          contentType: 'application/json',
          body: JSON.stringify({ error: 'Forbidden' }),
        });
        return;
      }
      await route.fallback();
    });

    await page.goto(pages.profile);
    await expect(page.locator('[data-delete]')).toHaveCount(2);

    await page.click('[data-delete="id-1"]');

    // Wait until both dialogs (confirm + alert) have been observed.
    await expect.poll(() => dialogs.length).toBe(2);
    expect(dialogs[0].type).toBe('confirm');
    expect(dialogs[1].type).toBe('alert');
    expect(dialogs[1].message).toBe('Could not delete the problem. You may not have permission.');

    // Row was not removed and the count was not changed.
    await expect(page.locator('[data-delete]')).toHaveCount(2);
    await expect(page.locator('[data-delete="id-1"]')).toHaveCount(1);
    await expect(page.locator('#profile-created-count')).toHaveText('2');
  });
});

// @ts-check
const { test, expect } = require('@playwright/test');
const path = require('path');

const pages = {
  index:    `file://${path.resolve(__dirname, '..', 'index.html')}`,
  login:    `file://${path.resolve(__dirname, '..', 'login.html')}`,
  problems: `file://${path.resolve(__dirname, '..', 'problems.html')}`,
  create:   `file://${path.resolve(__dirname, '..', 'create.html')}`,
  profile:  `file://${path.resolve(__dirname, '..', 'profile_page.html')}`,
};

/** @param {import('@playwright/test').Page} page */
function vw(page) { return page.viewportSize()?.width ?? 1280; }

// ── Navbar ────────────────────────────────────────────────────────────────────

test.describe('Navbar', () => {
  test('is visible on all viewports', async ({ page }) => {
    await page.goto(pages.index);
    await expect(page.locator('.navbar')).toBeVisible();
  });

  test('shows hamburger button and hides nav-links on mobile', async ({ page }) => {
    if (vw(page) > 640) test.skip();
    await page.goto(pages.index);
    await expect(page.locator('.nav-hamburger')).toBeVisible();
    await expect(page.locator('.nav-links')).toBeHidden();
  });

  test('hamburger toggles nav-links open/closed on mobile', async ({ page }) => {
    if (vw(page) > 640) test.skip();
    await page.goto(pages.index);
    const hamburger = page.locator('.nav-hamburger');
    const navLinks  = page.locator('.nav-links');

    await hamburger.click();
    await expect(navLinks).toBeVisible();

    await hamburger.click();
    await expect(navLinks).toBeHidden();
  });

  test('nav-links visible and no hamburger on tablet/desktop', async ({ page }) => {
    if (vw(page) <= 640) test.skip();
    await page.goto(pages.index);
    await expect(page.locator('.nav-links')).toBeVisible();
    await expect(page.locator('.nav-hamburger')).toBeHidden();
  });

  test('no horizontal overflow on navbar', async ({ page }) => {
    await page.goto(pages.index);
    const navScroll = await page.locator('.navbar').evaluate(el => el.scrollWidth);
    const bodyWidth = await page.evaluate(() => document.documentElement.clientWidth);
    expect(navScroll).toBeLessThanOrEqual(bodyWidth + 1);
  });
});

// ── Index page ────────────────────────────────────────────────────────────────

test.describe('Index page (hero)', () => {
  test('renders without horizontal scroll', async ({ page }) => {
    await page.goto(pages.index);
    const scroll = await page.evaluate(() => document.documentElement.scrollWidth);
    const width  = await page.evaluate(() => document.documentElement.clientWidth);
    expect(scroll).toBeLessThanOrEqual(width + 1);
  });

  test('hero title is visible', async ({ page }) => {
    await page.goto(pages.index);
    await expect(page.locator('.title')).toBeVisible();
  });

  test('hero grid is single column on mobile', async ({ page }) => {
    if (vw(page) > 768) test.skip();
    await page.goto(pages.index);
    const cols = await page.locator('.hero').evaluate(
      el => getComputedStyle(el).gridTemplateColumns
    );
    expect(cols.trim().split(/\s+(?=\d)/)).toHaveLength(1);
  });

  test('hero grid has two columns on desktop', async ({ page }) => {
    if (vw(page) <= 768) test.skip();
    await page.goto(pages.index);
    const cols = await page.locator('.hero').evaluate(
      el => getComputedStyle(el).gridTemplateColumns
    );
    expect(cols.trim().split(/\s+(?=\d)/).length).toBeGreaterThanOrEqual(2);
  });
});

// ── Login page ────────────────────────────────────────────────────────────────

test.describe('Login page', () => {
  test('renders without horizontal scroll', async ({ page }) => {
    await page.goto(pages.login);
    const scroll = await page.evaluate(() => document.documentElement.scrollWidth);
    const width  = await page.evaluate(() => document.documentElement.clientWidth);
    expect(scroll).toBeLessThanOrEqual(width + 1);
  });

  test('login card is visible and does not overflow viewport', async ({ page }) => {
    await page.goto(pages.login);
    const card = page.locator('.login-card');
    await expect(card).toBeVisible();
    const box       = await card.boundingBox() ?? { width: 0 };
    const viewWidth = await page.evaluate(() => document.documentElement.clientWidth);
    expect(box.width).toBeLessThanOrEqual(viewWidth);
  });

  test('sign-in button spans full width on mobile', async ({ page }) => {
    if (vw(page) > 640) test.skip();
    await page.goto(pages.login);
    const btnBox  = await page.locator('#login-btn').boundingBox();
    const cardBox = await page.locator('.login-card').boundingBox();
    const btnW  = (btnBox  ?? { width: 0 }).width;
    const cardW = (cardBox ?? { width: 0 }).width;
    expect(btnW).toBeGreaterThan(cardW * 0.8);
  });
});

// ── Problems page ─────────────────────────────────────────────────────────────

test.describe('Problems page', () => {
  test('renders without horizontal scroll', async ({ page }) => {
    await page.goto(pages.problems);
    const scroll = await page.evaluate(() => document.documentElement.scrollWidth);
    const width  = await page.evaluate(() => document.documentElement.clientWidth);
    expect(scroll).toBeLessThanOrEqual(width + 1);
  });

  test('grid is single column on mobile', async ({ page }) => {
    if (vw(page) > 768) test.skip();
    await page.goto(pages.problems);
    const cols = await page.locator('.problems-grid').evaluate(
      el => getComputedStyle(el).gridTemplateColumns
    );
    expect(cols.trim().split(/\s+(?=\d)/)).toHaveLength(1);
  });

  test('grid is single column on tablet', async ({ page }) => {
    if (vw(page) < 769 || vw(page) > 1024) test.skip();
    await page.goto(pages.problems);
    const cols = await page.locator('.problems-grid').evaluate(
      el => getComputedStyle(el).gridTemplateColumns
    );
    expect(cols.trim().split(/\s+(?=\d)/)).toHaveLength(1);
  });

  test('grid is single column on desktop', async ({ page }) => {
    if (vw(page) <= 1024) test.skip();
    await page.goto(pages.problems);
    const cols = await page.locator('.problems-grid').evaluate(
      el => getComputedStyle(el).gridTemplateColumns
    );
    expect(cols.trim().split(/\s+(?=\d)/)).toHaveLength(1);
  });

  test('problems header stacks on mobile', async ({ page }) => {
    if (vw(page) > 768) test.skip();
    await page.goto(pages.problems);
    const flexDir = await page.locator('.problems-header').evaluate(
      el => getComputedStyle(el).flexDirection
    );
    expect(flexDir).toBe('column');
  });
});

// ── Create page ───────────────────────────────────────────────────────────────

// create.js redirects to login if unauthenticated — inject a fake token first
async function gotoCreate(page) {
  await page.addInitScript(() => {
    localStorage.setItem('session_token', 'fake-test-token');
  });
  await page.goto(pages.create);
}

// profile_page.html redirects to login if unauthenticated — inject a fake token first
async function gotoProfile(page) {
  await page.addInitScript(() => {
    localStorage.setItem('session_token', 'fake-test-token');
  });
  await page.goto(pages.profile);
}

test.describe('Create page', () => {
  test('renders without horizontal scroll', async ({ page }) => {
    await gotoCreate(page);
    const scroll = await page.evaluate(() => document.documentElement.scrollWidth);
    const width  = await page.evaluate(() => document.documentElement.clientWidth);
    expect(scroll).toBeLessThanOrEqual(width + 1);
  });

  test('multi-column grids collapse to 1 column on mobile', async ({ page }) => {
    if (vw(page) > 768) test.skip();
    await gotoCreate(page);
    for (const cls of ['.grid-3-1', '.grid-1-1', '.grid-1-1-1']) {
      const el = page.locator(cls).first();
      if (await el.count() === 0) continue;
      const cols = await el.evaluate(el => getComputedStyle(el).gridTemplateColumns);
      expect(cols.trim().split(/\s+(?=\d)/)).toHaveLength(1);
    }
  });

  test('form actions stack on mobile', async ({ page }) => {
    if (vw(page) > 768) test.skip();
    await gotoCreate(page);
    const flexDir = await page.locator('.form-actions').evaluate(
      el => getComputedStyle(el).flexDirection
    );
    expect(flexDir).toBe('column-reverse');
  });
});

// ── Profile page ──────────────────────────────────────────────────────────────

test.describe('Profile page', () => {
  test('renders without horizontal scroll', async ({ page }) => {
    await gotoProfile(page);
    const scroll = await page.evaluate(() => document.documentElement.scrollWidth);
    const width  = await page.evaluate(() => document.documentElement.clientWidth);
    expect(scroll).toBeLessThanOrEqual(width + 1);
  });

  test('layout stacks to single column on mobile', async ({ page }) => {
    if (vw(page) > 768) test.skip();
    await gotoProfile(page);
    const cols = await page.locator('.profile-page').evaluate(
      el => getComputedStyle(el).gridTemplateColumns
    );
    expect(cols.trim().split(/\s+(?=\d)/)).toHaveLength(1);
  });

  test('stats row stacks on very small screens', async ({ page }) => {
    if (vw(page) > 480) test.skip();
    await gotoProfile(page);
    const cols = await page.locator('.stats-row').evaluate(
      el => getComputedStyle(el).gridTemplateColumns
    );
    expect(cols.trim().split(/\s+(?=\d)/)).toHaveLength(1);
  });

  test('layout is two columns on desktop', async ({ page }) => {
    if (vw(page) <= 768) test.skip();
    await gotoProfile(page);
    const cols = await page.locator('.profile-page').evaluate(
      el => getComputedStyle(el).gridTemplateColumns
    );
    expect(cols.trim().split(/\s+(?=\d)/).length).toBeGreaterThanOrEqual(2);
  });
});

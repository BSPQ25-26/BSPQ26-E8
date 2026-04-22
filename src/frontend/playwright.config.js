// @ts-check
const { defineConfig } = require('@playwright/test');
const path = require('path');

const FRONTEND_DIR = path.resolve(__dirname);

module.exports = defineConfig({
  testDir: './tests',
  timeout: 15000,
  retries: 0,
  reporter: [['list'], ['html', { open: 'never' }]],

  use: {
    // Serve files from the frontend directory
    baseURL: `file://${FRONTEND_DIR}/`,
    headless: true,
  },

  projects: [
    {
      name: 'mobile',
      use: {
        browserName: 'chromium',
        viewport: { width: 390, height: 844 },
      },
    },
    {
      name: 'tablet',
      use: {
        browserName: 'chromium',
        viewport: { width: 768, height: 1024 },
      },
    },
    {
      name: 'desktop',
      use: {
        browserName: 'chromium',
        viewport: { width: 1280, height: 800 },
      },
    },
  ],
});

/**
 * create.js — Problem creation form API integration
 *
 * Expects the following element IDs in the page:
 *   Form:                    id="create-form"
 *   Submit button:           id="submit-btn"
 *   Status/feedback element: id="status-message"
 *
 * Required fields (POST /api/problems):
 *   id="title"        — problem title
 *   id="statementMd"  — problem statement (Markdown)
 *   id="difficulty"   — select: EASY | MEDIUM | HARD
 *
 * Optional fields:
 *   id="slug"                      — URL slug (auto-generated from title if omitted)
 *   id="inputSpecMd"               — input specification (Markdown)
 *   id="outputSpecMd"              — output specification (Markdown)
 *   id="constraintsMd"             — constraints (Markdown)
 *   id="hintsMd"                   — hints (Markdown)
 *   id="solutionTemplate"          — starter code shown to solvers
 *   id="languageCompilationConfig" — JSON compilation config (defaults to {})
 *
 * Authentication:
 *   Uses api.js so expired access tokens are refreshed through POST /api/auth/refresh.
 *
 * Backend URL:
 *   Reads from window.API_BASE if defined, otherwise defaults to http://localhost:10000.
 *   Define it before this script loads to override: <script>window.API_BASE = '...';</script>
 */

(function () {
  'use strict';

  /** Returns trimmed string or null if blank */
  function field(id) {
    const el = document.getElementById(id);
    if (!el) return null;
    const val = el.value.trim();
    return val === '' ? null : val;
  }

  function showStatus(element, message, type) {
    element.textContent = message;
    element.dataset.status = type; // 'success' | 'error'
    element.style.display = 'block';
  }

  function hideStatus(element) {
    element.style.display = 'none';
    delete element.dataset.status;
  }

  async function submitCreateProblem(e) {
    e.preventDefault();

    const form      = document.getElementById('create-form');
    const submitBtn = document.getElementById('submit-btn');
    const statusEl  = document.getElementById('status-message');

    hideStatus(statusEl);

    // Auth check - auth.js stores the access and refresh tokens.
    if (!auth.isAuthenticated()) {
      showStatus(statusEl, 'You must be logged in to create a problem.', 'error');
      return;
    }

    // Required field validation
    const title       = field('title');
    const statementMd = field('statementMd');
    const difficulty  = field('difficulty');

    if (!title || !statementMd || !difficulty) {
      showStatus(statusEl, 'Title, statement and difficulty are required.', 'error');
      return;
    }

    const body = {
      title,
      statementMd,
      difficulty,
      slug:                     field('slug'),
      inputSpecMd:              field('inputSpecMd'),
      outputSpecMd:             field('outputSpecMd'),
      constraintsMd:            field('constraintsMd'),
      hintsMd:                  field('hintsMd'),
      solutionTemplate:         field('solutionTemplate'),
      languageCompilationConfig: field('languageCompilationConfig'),
    };

    submitBtn.disabled = true;
    const originalLabel = submitBtn.textContent;
    submitBtn.textContent = 'Publishing…';

    try {
      const data = await api.post('/problems', body);
      showStatus(statusEl, `Problem "${data.title}" created successfully!`, 'success');
      form.reset();
    } catch (err) {
      if (err && err.status === 401) {
        showStatus(statusEl, 'Session expired. Please log in again.', 'error');
      } else if (err && err.status === 409) {
        showStatus(statusEl, `Conflict: ${err.message || 'A problem with that slug already exists.'}`, 'error');
      } else if (!err || !err.status) {
        showStatus(statusEl, 'Could not reach the server. Is the backend running?', 'error');
      } else {
        showStatus(statusEl, `Error: ${(err && err.message) || 'An unexpected error occurred.'}`, 'error');
      }
    } finally {
      submitBtn.disabled = false;
      submitBtn.textContent = originalLabel;
    }
  }

  document.addEventListener('DOMContentLoaded', function () {
    auth.requireAuth(); // redirects to login.html if no token
    const form = document.getElementById('create-form');
    if (form) {
      form.addEventListener('submit', submitCreateProblem);
    }
  });
})();

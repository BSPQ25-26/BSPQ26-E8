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

  function getText(key, params) {
    return typeof i18n !== 'undefined' ? i18n.t(key, params) : key;
  }

  /** Returns trimmed string or null if blank */
  function field(id) {
    const el = document.getElementById(id);
    if (!el) return null;
    const val = el.value.trim();
    return val === '' ? null : val;
  }

  /** Builds a new example block element */
  function buildExampleBlock() {
    const block = document.createElement('div');
    block.className = 'example-box';
    block.dataset.example = '';
    block.innerHTML = `
      <div class="form-group">
        <input class="form-input" type="text" data-example-input placeholder="Input (e.g. nums = [2,7,11,15], target = 9)" />
      </div>
      <div class="form-group">
        <input class="form-input" type="text" data-example-output placeholder="Output (e.g. [0,1])" />
      </div>
      <div class="form-group">
        <input class="form-input" type="text" data-example-explanation placeholder="Explanation (optional)" />
      </div>
    `;
    return block;
  }

  /** Collects all filled-in examples from the examples container */
  function collectExamples() {
    const container = document.getElementById('examples-container');
    if (!container) return [];
    return Array.from(container.querySelectorAll('[data-example]'))
      .map((block) => ({
        inputData: (block.querySelector('[data-example-input]')?.value || '').trim(),
        expectedOutput: (block.querySelector('[data-example-output]')?.value || '').trim(),
      }))
      .filter((ex) => ex.inputData.length > 0);
  }

  /** Wires up the "+ Add" button for the examples section */
  function initExamples() {
    const container = document.getElementById('examples-container');
    const addBtn = document.getElementById('examples-add-btn');
    if (!container || !addBtn) return;
    addBtn.addEventListener('click', () => container.appendChild(buildExampleBlock()));
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
      showStatus(statusEl, getText('create.mustBeLoggedIn'), 'error');
      return;
    }

    // Required field validation
    const title       = field('title');
    const statementMd = field('statementMd');
    const difficulty  = field('difficulty');

    if (!title || !statementMd || !difficulty) {
      showStatus(statusEl, getText('create.requiredFields'), 'error');
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
      examples:                 collectExamples(),
    };

    submitBtn.disabled = true;
    const originalLabel = submitBtn.textContent;
    submitBtn.textContent = getText('create.publishing');

    try {
      const data = await api.post('/problems', body);
      showStatus(statusEl, getText('create.problemCreated', { title: data.title }), 'success');
      form.reset();
    } catch (err) {
      if (err && err.status === 401) {
        showStatus(statusEl, getText('create.sessionExpired'), 'error');
      } else if (err && err.status === 409) {
        showStatus(statusEl, getText('create.conflict', { message: err.message || getText('create.problemAlreadyExists') }), 'error');
      } else if (!err || !err.status) {
        showStatus(statusEl, getText('create.couldNotReachServer'), 'error');
      } else {
        showStatus(statusEl, `${getText('common.error')}: ${(err && err.message) || getText('create.unexpectedError')}`, 'error');
      }
    } finally {
      submitBtn.disabled = false;
      submitBtn.textContent = originalLabel;
    }
  }

  document.addEventListener('DOMContentLoaded', function () {
    auth.requireAuth(); // redirects to login.html if no token
    initExamples();
    const form = document.getElementById('create-form');
    if (form) {
      form.addEventListener('submit', submitCreateProblem);
    }
  });
})();

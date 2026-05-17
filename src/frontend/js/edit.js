(function () {
  'use strict';

  const params = new URLSearchParams(window.location.search);
  const problemId = params.get('id');

  /* ── Field helpers ── */
  function field(id) {
    const el = document.getElementById(id);
    if (!el) return null;
    const val = el.value.trim();
    return val === '' ? null : val;
  }

  function setField(id, value) {
    const el = document.getElementById(id);
    if (el && value != null) el.value = value;
  }

  function showStatus(element, message, type) {
    element.textContent = message;
    element.dataset.status = type;
    element.style.display = 'block';
  }

  function hideStatus(element) {
    element.style.display = 'none';
    delete element.dataset.status;
  }

  /* ── Examples ── */
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

  function initExamples() {
    const container = document.getElementById('examples-container');
    const addBtn = document.getElementById('examples-add-btn');
    if (!container || !addBtn) return;
    addBtn.addEventListener('click', () => container.appendChild(buildExampleBlock()));
  }

  /* ── Load problem data and pre-fill form ── */
  async function loadProblem() {
    if (!problemId) {
      window.location.href = 'profile_page.html';
      return;
    }

    try {
      const data = await api.get(`/problems/${problemId}`);

      setField('title', data.title);
      setField('statementMd', data.statementMd);
      setField('difficulty', data.difficulty);
      setField('constraintsMd', data.constraintsMd);
      setField('solutionTemplate', data.solutionTemplate);

      // Pre-fill examples
      if (Array.isArray(data.examples) && data.examples.length > 0) {
        const container = document.getElementById('examples-container');
        if (container) {
          container.innerHTML = '';
          data.examples.forEach((ex) => {
            const block = buildExampleBlock();
            block.querySelector('[data-example-input]').value = ex.inputData || '';
            block.querySelector('[data-example-output]').value = ex.expectedOutput || '';
            container.appendChild(block);
          });
        }
      }
    } catch (_err) {
      const statusEl = document.getElementById('status-message');
      if (statusEl) showStatus(statusEl, 'Could not load the problem. Please go back and try again.', 'error');
    }
  }

  /* ── Submit edit ── */
  async function submitEditProblem(e) {
    e.preventDefault();

    const submitBtn = document.getElementById('submit-btn');
    const statusEl  = document.getElementById('status-message');

    hideStatus(statusEl);

    if (!auth.isAuthenticated()) {
      showStatus(statusEl, 'You must be logged in to edit a problem.', 'error');
      return;
    }

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
      constraintsMd:    field('constraintsMd'),
      solutionTemplate: field('solutionTemplate'),
    };

    submitBtn.disabled = true;
    const originalLabel = submitBtn.textContent;
    submitBtn.textContent = 'Saving…';

    try {
      await api.put(`/problems/${problemId}`, body);
      showStatus(statusEl, 'Problem updated successfully!', 'success');
    } catch (err) {
      if (err && err.status === 401) {
        showStatus(statusEl, 'Session expired. Please log in again.', 'error');
      } else if (err && err.status === 403) {
        showStatus(statusEl, 'You are not allowed to edit this problem.', 'error');
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

  /* ── Init ── */
  document.addEventListener('DOMContentLoaded', function () {
    auth.requireAuth();
    initExamples();
    loadProblem();

    const form = document.getElementById('create-form');
    if (form) {
      form.addEventListener('submit', submitEditProblem);
    }
  });
})();

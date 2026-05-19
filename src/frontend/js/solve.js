(function () {
  'use strict';

  /* ── URL param ── */
  const params = new URLSearchParams(window.location.search);
  const problemId = params.get('id');

  /* ── Default code templates per language ── */
  const TEMPLATES = {
    python:
      'class Solution:\n    def solve(self, ):\n        pass\n',
    java:
      'class Solution {\n    public void solve() {\n        \n    }\n}\n',
    cpp:
      'class Solution {\npublic:\n    void solve() {\n        \n    }\n};\n',
    javascript:
      '/**\n * @return {void}\n */\nvar solve = function() {\n    \n};\n',
  };

  /* ── State ── */
  let problemData = null;

  /* ── Element references ── */
  const tabButtons    = document.querySelectorAll('.solve-tab');
  const tabDesc       = document.getElementById('tab-description');
  const tabExamples   = document.getElementById('tab-examples');
  const tabHints      = document.getElementById('tab-hints');
  const problemHeader = document.getElementById('problem-header');
  const problemStmt   = document.getElementById('problem-statement');
  const problemEx     = document.getElementById('problem-examples');
  const problemHints  = document.getElementById('problem-hints');
  const langSelect    = document.getElementById('lang-select');
  const codeEditor    = document.getElementById('code-editor');
  const btnRun        = document.getElementById('btn-run');
  const btnSubmit     = document.getElementById('btn-submit');
  const outputPanel   = document.getElementById('solve-output');
  const outputStatus  = document.getElementById('output-status');
  const outputBody    = document.getElementById('output-body');
  const btnClose      = document.getElementById('btn-close-output');
  const divider       = document.getElementById('solve-divider');
  const leftPanel     = document.querySelector('.solve-panel--desc');

  /* ── Tabs ── */
  const TABS = {
    description: tabDesc,
    examples:    tabExamples,
    hints:       tabHints,
  };

  tabButtons.forEach((btn) => {
    btn.addEventListener('click', () => {
      tabButtons.forEach((b) => b.classList.remove('is-active'));
      btn.classList.add('is-active');
      Object.values(TABS).forEach((el) => (el.style.display = 'none'));
      const target = TABS[btn.dataset.tab];
      if (target) target.style.display = 'block';
    });
  });

  /* ── Escape HTML ── */
  function esc(str) {
    return String(str || '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  /* ── Render left panel ── */
  function renderProblem(data) {
    problemData = data;

    /* Header: title + difficulty */
    const diffMap = {
      EASY:   'badge--easy',
      MEDIUM: 'badge--medium',
      HARD:   'badge--hard',
    };
    const badgeClass = diffMap[data.difficulty] || 'badge--blue';

    problemHeader.innerHTML = `
      <div class="solve-problem-header">
        <h1 class="solve-problem-title">${esc(data.title)}</h1>
        <div class="solve-problem-meta">
          <span class="badge ${badgeClass}">${esc(data.difficulty)}</span>
        </div>
      </div>`;

    /* Statement */
    const sections = [];
    if (data.statementMd) {
      sections.push(`
        <div class="solve-section">
          <span class="solve-section-title">Description</span>
          <p class="solve-section-body">${esc(data.statementMd)}</p>
        </div>`);
    }
    if (data.inputSpecMd) {
      sections.push(`
        <div class="solve-section">
          <span class="solve-section-title">Input</span>
          <p class="solve-section-body">${esc(data.inputSpecMd)}</p>
        </div>`);
    }
    if (data.outputSpecMd) {
      sections.push(`
        <div class="solve-section">
          <span class="solve-section-title">Output</span>
          <p class="solve-section-body">${esc(data.outputSpecMd)}</p>
        </div>`);
    }
    if (data.constraintsMd) {
      sections.push(`
        <div class="solve-section">
          <span class="solve-section-title">Constraints</span>
          <p class="solve-section-body">${esc(data.constraintsMd)}</p>
        </div>`);
    }
    problemStmt.innerHTML = sections.join('');

    /* Examples tab */
    if (Array.isArray(data.examples) && data.examples.length > 0) {
      const items = data.examples.map((ex, i) => `
        <div class="solve-example">
          <div class="solve-example-label">Example ${i + 1}</div>
          <div class="solve-example-row">
            <div class="solve-example-block">
              <div class="solve-example-block-label">Input</div>
              <pre class="solve-example-code">${esc(ex.inputData)}</pre>
            </div>
            <div class="solve-example-block">
              <div class="solve-example-block-label">Output</div>
              <pre class="solve-example-code">${esc(ex.expectedOutput)}</pre>
            </div>
          </div>
        </div>`).join('');
      problemEx.innerHTML = `<div class="solve-examples">${items}</div>`;
    } else {
      problemEx.innerHTML = '<p class="solve-hints-empty">No examples available.</p>';
    }

    /* Hints tab */
    if (data.hintsMd) {
      problemHints.innerHTML = `
        <div class="solve-section">
          <span class="solve-section-title">Hints</span>
          <p class="solve-section-body">${esc(data.hintsMd)}</p>
        </div>`;
    }

    /* Pre-fill editor with solutionTemplate or default */
    setEditorTemplate();

    /* Page title */
    document.title = `${data.title} — Realcode`;
  }

  /* ── Editor template ── */
  function setEditorTemplate() {
    if (!problemData) return;
    const lang = langSelect.value;
    // Use the problem's solutionTemplate if available, otherwise use default
    const template = problemData.solutionTemplate || TEMPLATES[lang] || '';
    codeEditor.value = template;
  }

  langSelect.addEventListener('change', setEditorTemplate);

  /* ── Tab key in editor (insert spaces) ── */
  codeEditor.addEventListener('keydown', (e) => {
    if (e.key === 'Tab') {
      e.preventDefault();
      const start = codeEditor.selectionStart;
      const end = codeEditor.selectionEnd;
      codeEditor.value =
        codeEditor.value.substring(0, start) + '    ' + codeEditor.value.substring(end);
      codeEditor.selectionStart = codeEditor.selectionEnd = start + 4;
    }
  });

  /* ── Output panel helpers ── */
  function showOutput(text, statusText, statusClass) {
    outputBody.textContent = text;
    outputStatus.textContent = statusText;
    outputStatus.className = `solve-output-status ${statusClass}`;
    outputPanel.style.display = 'flex';
  }

  function hideOutput() {
    outputPanel.style.display = 'none';
  }

  btnClose.addEventListener('click', hideOutput);

  /* ── Run ── */
  btnRun.addEventListener('click', async () => {
    if (!problemId) return;
    const code = codeEditor.value;
    const language = langSelect.value;

    showOutput('Running…', 'Running', 'running');
    btnRun.disabled = true;

    try {
      const result = await api.runCode(problemId, code, language);
      const output = result.output || result.stdout || JSON.stringify(result, null, 2);
      const isError = result.status === 'ERROR' || result.stderr;
      showOutput(
        isError ? (result.stderr || output) : output,
        isError ? 'Error' : 'Finished',
        isError ? 'error' : 'accepted'
      );
    } catch (err) {
      showOutput(
        err && err.message ? err.message : 'Could not reach the server.',
        'Error',
        'error'
      );
    } finally {
      btnRun.disabled = false;
    }
  });

  /* ── Submit ── */
  btnSubmit.addEventListener('click', async () => {
    if (!problemId) return;
    const code = codeEditor.value;
    const language = langSelect.value;

    showOutput('Submitting…', 'Running', 'running');
    btnSubmit.disabled = true;

    try {
      const result = await api.submitSolution(problemId, code, language);
      const accepted = result.status === 'ACCEPTED' || result.accepted === true;
      const output = result.output || result.message || JSON.stringify(result, null, 2);
      showOutput(output, accepted ? 'Accepted' : 'Wrong Answer', accepted ? 'accepted' : 'error');
    } catch (err) {
      showOutput(
        err && err.message ? err.message : 'Could not reach the server.',
        'Error',
        'error'
      );
    } finally {
      btnSubmit.disabled = false;
    }
  });

  /* ── Draggable divider ── */
  if (divider && leftPanel) {
    let dragging = false;
    let startX = 0;
    let startWidth = 0;

    divider.addEventListener('mousedown', (e) => {
      dragging = true;
      startX = e.clientX;
      startWidth = leftPanel.getBoundingClientRect().width;
      divider.classList.add('is-dragging');
      document.body.style.cursor = 'col-resize';
      document.body.style.userSelect = 'none';
    });

    document.addEventListener('mousemove', (e) => {
      if (!dragging) return;
      const delta = e.clientX - startX;
      const newWidth = Math.min(Math.max(startWidth + delta, 280), window.innerWidth - 400);
      leftPanel.style.width = `${newWidth}px`;
    });

    document.addEventListener('mouseup', () => {
      if (!dragging) return;
      dragging = false;
      divider.classList.remove('is-dragging');
      document.body.style.cursor = '';
      document.body.style.userSelect = '';
    });
  }

  /* ── Load problem ── */
  async function loadProblem() {
    if (!problemId) {
      problemHeader.innerHTML = '<p class="solve-error">No problem selected. <a href="problems.html">Go back</a></p>';
      return;
    }

    try {
      const data = await api.get(`/problems/${problemId}`);
      renderProblem(data);
    } catch (_err) {
      problemHeader.innerHTML = '<p class="solve-error">Could not load problem. Please go back and try again.</p>';
    }
  }

  /* ── Init ── */
  document.addEventListener('DOMContentLoaded', () => {
    loadProblem();
  });

})();

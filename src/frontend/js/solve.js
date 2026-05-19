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

  /* ── Run output renderer ── */
  const STATUS_META = {
    ACCEPTED:              { pill: 'accepted', label: 'Accepted' },
    WRONG_ANSWER:          { pill: 'wrong',    label: 'Wrong Answer' },
    RUNTIME_ERROR:         { pill: 'error',    label: 'Runtime Error' },
    COMPILE_ERROR:         { pill: 'error',    label: 'Compile Error' },
    TIME_LIMIT_EXCEEDED:   { pill: 'limit',    label: 'Time Limit Exceeded' },
    MEMORY_LIMIT_EXCEEDED: { pill: 'limit',    label: 'Memory Limit Exceeded' },
    INTERNAL_ERROR:        { pill: 'internal', label: 'Internal Error' },
    QUEUED:                { pill: 'running',  label: 'Queued' },
    RUNNING:               { pill: 'running',  label: 'Running' },
  };

  function normalizeOutput(s) {
    return String(s == null ? '' : s)
      .replace(/\r\n/g, '\n')
      .replace(/\r/g, '\n')
      .replace(/\s+$/g, '');
  }

  // Mirrors SubmissionEvaluator's per-case mapping; needed because PreviewExecutionView
  // exposes judge0StatusId + stdout/expected, not a pre-computed per-case status.
  function caseVerdict(tc) {
    if (tc.compileOutput && tc.compileOutput.trim()) return 'COMPILE_ERROR';
    const id = tc.judge0StatusId;
    if (id === 5)  return 'TIME_LIMIT_EXCEEDED';
    if (id === 6)  return 'COMPILE_ERROR';
    if (id === 13) return 'INTERNAL_ERROR';
    if (id != null && id >= 7 && id <= 12) return 'RUNTIME_ERROR';
    if (id === 14) return 'RUNTIME_ERROR';
    if (id === 4)  return 'WRONG_ANSWER';
    if (id === 3) {
      return normalizeOutput(tc.stdout) === normalizeOutput(tc.expectedOutput)
        ? 'ACCEPTED'
        : 'WRONG_ANSWER';
    }
    return 'INTERNAL_ERROR';
  }

  function formatCaseTime(tc) {
    if (tc.time) {
      const ms = Math.round(parseFloat(tc.time) * 1000);
      if (!Number.isNaN(ms)) return ms + ' ms';
    }
    return '—';
  }

  function formatCaseMem(tc) {
    if (tc.memoryKb != null && tc.memoryKb > 0) {
      return Math.ceil(tc.memoryKb / 1024) + ' MB';
    }
    return '—';
  }

  function renderRunOutput(view, language) {
    const meta = STATUS_META[view.status] || { pill: 'internal', label: view.status || 'Unknown' };
    outputStatus.textContent = meta.label;
    outputStatus.className = `solve-output-status ${meta.pill}`;
    outputPanel.style.display = 'flex';

    if (view.failed) {
      outputBody.innerHTML = `
        <div class="run-output">
          <div class="run-output-cmdline">
            <span class="run-output-prompt">$</span>
            <span class="run-output-cmd">run sample-tests</span>
          </div>
          <pre class="run-output-error">${esc(view.errorMessage || 'Execution failed')}</pre>
        </div>`;
      return;
    }

    const cases   = Array.isArray(view.testCases) ? view.testCases : [];
    const runtime = view.runtimeMs != null ? view.runtimeMs + ' ms' : '—';
    const memory  = view.memoryMb  != null ? view.memoryMb  + ' MB' : '—';
    const passed  = view.testcasesPassed;
    const total   = view.testcasesTotal;

    const head = `
      <div class="run-output-cmdline">
        <span class="run-output-prompt">$</span>
        <span class="run-output-cmd">run sample-tests</span>
        <span class="run-output-cmd-meta">— ${esc(language)} · ${cases.length} case${cases.length === 1 ? '' : 's'}</span>
      </div>
      <div class="run-output-metrics">
        <span>time: ${runtime}</span>
        <span>mem: ${memory}</span>
        <span>pass: ${passed} / ${total}</span>
      </div>`;

    const caseBlocks = cases.map((tc, i) => {
      const verdict     = caseVerdict(tc);
      const verdictMeta = STATUS_META[verdict] || { label: verdict };
      const pass        = verdict === 'ACCEPTED';
      const cls         = `run-case run-case--${pass ? 'pass' : 'fail'}`;
      const openAttr    = pass ? '' : 'open';
      const num         = (tc.index != null ? tc.index : i) + 1;

      const rows = [];
      if (tc.inputData != null && tc.inputData !== '') {
        rows.push(`
          <div class="run-case-row">
            <span class="run-case-label">stdin  &gt;</span>
            <pre>${esc(tc.inputData)}</pre>
          </div>`);
      }
      rows.push(`
        <div class="run-case-row">
          <span class="run-case-label">expect &gt;</span>
          <pre>${esc(tc.expectedOutput || '')}</pre>
        </div>`);
      rows.push(`
        <div class="run-case-row">
          <span class="run-case-label">stdout &gt;</span>
          <pre>${esc(tc.stdout || '')}</pre>
        </div>`);
      if (tc.stderr && tc.stderr.trim()) {
        rows.push(`
          <div class="run-case-row">
            <span class="run-case-label">stderr &gt;</span>
            <pre class="run-case-stderr">${esc(tc.stderr)}</pre>
          </div>`);
      }
      if (tc.compileOutput && tc.compileOutput.trim()) {
        rows.push(`
          <div class="run-case-row">
            <span class="run-case-label">compile&gt;</span>
            <pre class="run-case-stderr">${esc(tc.compileOutput)}</pre>
          </div>`);
      }

      return `
        <details class="${cls}" ${openAttr}>
          <summary>
            <span class="run-case-name">case ${num}</span>
            <span class="run-case-badge">${pass ? 'PASS' : 'FAIL'}</span>
            <span class="run-case-verdict">${esc(verdictMeta.label)}</span>
            <span class="run-case-time">${formatCaseTime(tc)} · ${formatCaseMem(tc)}</span>
          </summary>
          <div class="run-case-detail">${rows.join('')}</div>
        </details>`;
    }).join('');

    const body = cases.length
      ? `<div class="run-output-cases">${caseBlocks}</div>`
      : '<div class="run-output-empty">No sample test cases.</div>';

    outputBody.innerHTML = `<div class="run-output">${head}${body}</div>`;
  }

  /* ── Run ── */
  btnRun.addEventListener('click', async () => {
    if (!problemId) return;
    const code = codeEditor.value;
    const language = langSelect.value;

    showOutput('Running…', 'Running', 'running');
    btnRun.disabled = true;

    try {
      const result = await api.runCode(problemId, code, language);
      renderRunOutput(result, language);
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
      const status = String(result.status || '').toUpperCase();
      const output = result.verdictMessage || result.output || result.message || JSON.stringify(result, null, 2);

      if (status === 'ACCEPTED' || result.accepted === true) {
        showOutput(output, 'Accepted', 'accepted');
      } else if (status === 'WRONG_ANSWER') {
        showOutput(output, 'Wrong Answer', 'error');
      } else if (status === 'QUEUED' || status === 'RUNNING') {
        showOutput(output, 'Submitted', 'running');
      } else {
        showOutput(output, status || 'Submitted', 'running');
      }
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

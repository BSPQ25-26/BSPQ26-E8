(function () {
  'use strict';

  function getText(key, params) {
    return typeof i18n !== 'undefined' ? i18n.t(key, params) : key;
  }

  const API_BASE = window.API_BASE || 'http://localhost:10000';

  const grid = document.getElementById('problems-grid');
  const statusEl = document.getElementById('problems-status');
  const countEl = document.getElementById('problems-count');
  const difficultyEl = document.getElementById('filter-difficulty');
  const languageEl = document.getElementById('filter-language');
  const sortEl = document.getElementById('filter-sort');
  const searchEl = document.getElementById('search-name');
  const clearFiltersEl = document.getElementById('clear-filters');

  if (!grid || !statusEl || !countEl || !difficultyEl || !languageEl || !sortEl || !searchEl || !clearFiltersEl) {
    return;
  }

  const DIFFICULTY = {
    EASY: { card: 'card--green', badge: 'badge--easy', labelKey: 'problems.difficultyEasy' },
    MEDIUM: { card: 'card--orange', badge: 'badge--medium', labelKey: 'problems.difficultyMedium' },
    HARD: { card: 'card--red', badge: 'badge--hard', labelKey: 'problems.difficultyHard' },
  };

  const state = {
    search: '',
    difficulty: '',
    language: '',
    sort: 'difficulty_name',
    activeController: null,
    requestCounter: 0,
    loadedProblems: [],
  };

  const detailCache = new Map();

  let searchTimer = null;

  function setStatus(message, isError = false) {
    statusEl.textContent = message;
    statusEl.className = 'problems-status' + (isError ? ' error' : '');
  }

  function clearStatus() {
    statusEl.textContent = '';
    statusEl.className = 'problems-status';
  }

  function setCount(total) {
    countEl.textContent = getText('problems.count', { count: total });
  }

  function formatCreatedAt(isoDate) {
    if (!isoDate) return getText('common.unknownDate');
    const date = new Date(isoDate);
    if (Number.isNaN(date.getTime())) return getText('common.unknownDate');
    const locale = typeof i18n !== 'undefined' ? i18n.getLocale() : 'en';
    return getText('problems.createdAt', { date: date.toLocaleDateString(locale) });
  }

  function formatLanguages(languages) {
    if (!Array.isArray(languages) || languages.length === 0) {
      return getText('problems.languageNotSpecified');
    }

    const visible = languages
      .filter((value) => typeof value === 'string' && value.trim().length > 0)
      .map((value) => value.trim());
    if (visible.length === 0) {
      return getText('problems.languageNotSpecified');
    }

    if (visible.length === 1) {
      return getText('problems.languageSingular', { language: visible[0] });
    }

    return getText('problems.languagePlural', { languages: visible.join(', ') });
  }

  function buildParams() {
    const params = new URLSearchParams();
    if (state.search) params.set('name', state.search);
    if (state.difficulty) params.set('difficulty', state.difficulty);
    if (state.language) params.set('language', state.language);
    return params;
  }

  function clearAllFilters() {
    state.search = '';
    state.difficulty = '';
    state.language = '';
    state.sort = 'difficulty_name';
    searchEl.value = '';
    difficultyEl.value = '';
    languageEl.value = '';
    sortEl.value = 'difficulty_name';
  }

  function getDifficultyWeight(difficulty) {
    if (difficulty === 'EASY') return 0;
    if (difficulty === 'MEDIUM') return 1;
    if (difficulty === 'HARD') return 2;
    return 3;
  }

  function sortProblems(problems) {
    const sorted = [...problems];

    if (state.sort === 'alphabetical') {
      sorted.sort((a, b) => (a.title || '').localeCompare(b.title || ''));
      return sorted;
    }

    if (state.sort === 'created_at') {
      sorted.sort((a, b) => new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime());
      return sorted;
    }

    sorted.sort((a, b) => {
      const difficultyDiff = getDifficultyWeight(a.difficulty) - getDifficultyWeight(b.difficulty);
      if (difficultyDiff !== 0) return difficultyDiff;
      return (a.title || '').localeCompare(b.title || '');
    });
    return sorted;
  }

  function escapeHtml(str) {
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  function buildDetailSection(title, body) {
    return `
      <div class="problem-detail-section">
        <span class="problem-detail-section-title">${title}</span>
        <p class="problem-detail-section-body">${escapeHtml(body)}</p>
      </div>`;
  }

  function renderDetail(detailEl, data) {
    const sections = [];

    if (data.statementMd) sections.push(buildDetailSection(getText('problems.problemStatement'), data.statementMd));
    if (data.inputSpecMd) sections.push(buildDetailSection(getText('problems.input'), data.inputSpecMd));
    if (data.outputSpecMd) sections.push(buildDetailSection(getText('problems.output'), data.outputSpecMd));
    if (data.constraintsMd) sections.push(buildDetailSection(getText('problems.constraints'), data.constraintsMd));
    if (data.hintsMd) sections.push(buildDetailSection(getText('problems.hints'), data.hintsMd));

    let examplesHtml = '';
    if (Array.isArray(data.examples) && data.examples.length > 0) {
      const items = data.examples.map((ex) => `
        <div class="problem-detail-example">
          <div class="problem-detail-example-block">
            <span class="problem-detail-example-label">Input</span>
            <pre class="problem-detail-example-code">${escapeHtml(ex.inputData || '')}</pre>
          </div>
          <div class="problem-detail-example-block">
            <span class="problem-detail-example-label">Output</span>
            <pre class="problem-detail-example-code">${escapeHtml(ex.expectedOutput || '')}</pre>
          </div>
        </div>`).join('');
      examplesHtml = `
        <div class="problem-detail-section">
          <span class="problem-detail-section-title">${getText('problems.examples')}</span>
          <div class="problem-detail-examples">${items}</div>
        </div>`;
    }

    detailEl.innerHTML = `
      ${sections.join('')}
      ${examplesHtml}
      <div class="problem-detail-footer">
        <button type="button" class="problem-detail-start-btn">${getText('problems.startCoding')}</button>
      </div>`;

    detailEl.querySelector('.problem-detail-start-btn').addEventListener('click', (e) => {
      e.stopPropagation();
    });
  }

  async function toggleDetail(card, problem) {
    const detailEl = card.querySelector('.problem-detail');

    if (card.classList.contains('is-expanded')) {
      card.classList.remove('is-expanded');
      detailEl.classList.remove('is-open');
      return;
    }

    card.classList.add('is-expanded');
    detailEl.classList.add('is-open');

    if (detailCache.has(problem.id)) {
      renderDetail(detailEl, detailCache.get(problem.id));
      return;
    }

    detailEl.innerHTML = `<p class="problem-detail-loading">${getText('problems.loadingDetails')}</p>`;

    try {
      const response = await fetch(`${API_BASE}/api/problems/${problem.id}`);
      if (!response.ok) {
        detailEl.innerHTML = `<p class="problem-detail-error">${getText('problems.couldNotLoadDetails')}</p>`;
        return;
      }
      const data = await response.json();
      detailCache.set(problem.id, data);
      renderDetail(detailEl, data);
    } catch (err) {
      detailEl.innerHTML = `<p class="problem-detail-error">${getText('problems.couldNotReachServer')}</p>`;
    }
  }

  function buildCard(problem) {
    const diff = DIFFICULTY[problem.difficulty] || DIFFICULTY.EASY;
    const safeSlug = problem.slug || 'no-slug';
    const safeTitle = problem.title || 'Untitled problem';

    const card = document.createElement('div');
    card.className = `card ${diff.card} problem-card`;

    // Restored to your original vertical layout
    card.innerHTML = `
      <div class="problem-card-summary">
        <div class="problem-card-main">
          <!-- Slug removed here -->
          <span class="problem-card-title">${safeTitle}</span>
          <span class="problem-card-language">${formatLanguages(problem.languages)}</span>
        </div>
        <div class="problem-card-meta-group">
          <span class="problem-card-meta">${formatCreatedAt(problem.createdAt)}</span>
          <span class="badge ${diff.badge}">${getText(diff.labelKey)}</span>
          <span class="problem-card-toggle">▼</span>
        </div>
      </div>
      <div class="problem-detail"></div>
    `;

    card.addEventListener('click', () => toggleDetail(card, problem));
    return card;
  }

  function renderProblems(problems) {
    grid.innerHTML = '';
    setCount(problems.length);

    if (problems.length === 0) {
      setStatus(getText('problems.noMatch'));
      return;
    }

    clearStatus();
    problems.forEach((problem) => grid.appendChild(buildCard(problem)));
  }

  async function fetchProblems() {
    state.requestCounter += 1;
    const requestId = state.requestCounter;

    if (state.activeController) {
      state.activeController.abort();
    }
    state.activeController = new AbortController();

    setStatus(getText('problems.loading'));
    grid.innerHTML = '';
    setCount(0);

    const params = buildParams();

    try {
      const response = await fetch(`${API_BASE}/api/problems?${params.toString()}`, {
        signal: state.activeController.signal,
      });

      if (requestId !== state.requestCounter) {
        return;
      }

      if (!response.ok) {
        setStatus(getText('problems.failedLoad'), true);
        return;
      }

      const problems = await response.json();
      if (!Array.isArray(problems)) {
        setStatus(getText('problems.invalidDataset'), true);
        return;
      }
      state.loadedProblems = problems;
      renderProblems(sortProblems(problems));
    } catch (err) {
      if (err && err.name === 'AbortError') {
        return;
      }
      setStatus(getText('problems.couldNotReachServer'), true);
    }
  }

  difficultyEl.addEventListener('change', () => {
    state.difficulty = difficultyEl.value;
    fetchProblems();
  });

  languageEl.addEventListener('change', () => {
    state.language = languageEl.value;
    fetchProblems();
  });

  sortEl.addEventListener('change', () => {
    state.sort = sortEl.value;
    renderProblems(sortProblems(state.loadedProblems));
  });

  searchEl.addEventListener('input', () => {
    if (searchTimer) clearTimeout(searchTimer);
    state.search = searchEl.value.trim();
    searchTimer = setTimeout(fetchProblems, 250);
  });

  clearFiltersEl.addEventListener('click', () => {
    clearAllFilters();
    searchEl.focus();
    fetchProblems();
  });

  fetchProblems();

  document.addEventListener('realcode:localechange', function () {
    fetchProblems();
  });

})();
(function () {
  'use strict';

  const API_BASE = window.API_BASE || 'http://localhost:8080';

  const grid      = document.getElementById('problems-grid');
  const statusEl  = document.getElementById('problems-status');
  const filterEl  = document.getElementById('filter-difficulty');

  // Maps difficulty value to card colour modifier and badge class from base.css
  const DIFFICULTY = {
    EASY:   { card: 'card--green',  badge: 'badge--easy',   label: 'Easy'   },
    MEDIUM: { card: 'card--orange', badge: 'badge--medium', label: 'Medium' },
    HARD:   { card: 'card--red',    badge: 'badge--hard',   label: 'Hard'   },
  };

  function setStatus(message, isError) {
    statusEl.textContent = message;
    statusEl.className = 'problems-status' + (isError ? ' error' : '');
  }

  function clearStatus() {
    statusEl.textContent = '';
    statusEl.className = 'problems-status';
  }

  function buildCard(problem) {
    const diff = DIFFICULTY[problem.difficulty] || DIFFICULTY.EASY;

    const card = document.createElement('div');
    card.className = `card ${diff.card} problem-card`;

    card.innerHTML = `
      <div class="problem-card-top">
        <span class="problem-card-slug">${problem.slug}</span>
        <span class="badge ${diff.badge}">${diff.label}</span>
      </div>
      <p class="problem-card-title">${problem.title}</p>
    `;

    return card;
  }

  function renderProblems(problems) {
    grid.innerHTML = '';

    if (problems.length === 0) {
      setStatus('No problems found.');
      return;
    }

    clearStatus();
    problems.forEach(p => grid.appendChild(buildCard(p)));
  }

  async function fetchProblems(difficulty) {
    setStatus('Loading…');
    grid.innerHTML = '';

    const params = new URLSearchParams();
    if (difficulty) params.set('difficulty', difficulty);

    try {
      const response = await fetch(`${API_BASE}/api/problems?${params}`);

      if (!response.ok) {
        setStatus('Failed to load problems.', true);
        return;
      }

      const problems = await response.json();
      renderProblems(problems);
    } catch (_err) {
      setStatus('Could not reach the server. ', true);
    }
  }

  filterEl.addEventListener('change', () => fetchProblems(filterEl.value));

  // Initial load — all problems
  fetchProblems('');

})();

/**
 * Shared page chrome renderer.
 * Injects accent bar, navbar and bottom strip so pages reuse one structure.
 */
(function () {
  'use strict';

  function navLink(href, label, activeKey, currentKey) {
    const isActive = activeKey === currentKey ? ' class="active"' : '';
    return `<li><a href="${href}"${isActive}>${label}</a></li>`;
  }

  function buildNavbar(activeKey) {
    const links = [
      navLink('problems.html', 'Problems', activeKey, 'problems'),
      navLink('create.html', 'Create', activeKey, 'create'),
    ];

    const isAuthenticated = typeof auth !== 'undefined' ? auth.isAuthenticated() : localStorage.getItem('session_token') !== null;

    if (isAuthenticated) {
      links.push(
        '<li><a class="nav-avatar-link" href="profile_page.html" aria-label="Open profile">' +
          '<img class="nav-avatar" src="https://i.pravatar.cc/96?img=12" alt="Profile avatar" />' +
        '</a></li>'
      );
    } else {
      links.push(
        '<li><a class="nav-login-link" href="login.html" aria-label="Log in">' +
          'Log In' +
        '</a></li>'
      );
    }

    return `
      <nav class="navbar">
        <a href="index.html" class="nav-logo">Realcode</a>
        <ul class="nav-links">${links.join('')}</ul>
        <button class="nav-hamburger" aria-label="Toggle navigation" aria-expanded="false">
          <span></span><span></span><span></span>
        </button>
      </nav>
    `;
  }

  function setupHamburger() {
    const navbar = document.querySelector('.navbar');
    const btn = navbar && navbar.querySelector('.nav-hamburger');
    if (!btn) return;

    btn.addEventListener('click', function () {
      const isOpen = navbar.classList.toggle('nav-open');
      btn.setAttribute('aria-expanded', String(isOpen));
    });

    // Close menu when a nav link is clicked
    const navLinks = navbar.querySelectorAll('.nav-links a');
    navLinks.forEach(function (link) {
      link.addEventListener('click', function () {
        navbar.classList.remove('nav-open');
        btn.setAttribute('aria-expanded', 'false');
      });
    });

    // Close menu when clicking outside
    document.addEventListener('click', function (e) {
      if (!navbar.contains(e.target)) {
        navbar.classList.remove('nav-open');
        btn.setAttribute('aria-expanded', 'false');
      }
    });
  }

  function injectLayout() {
    const body = document.body;
    if (!body) return;

    const activeNav = body.dataset.activeNav || '';
    const bottomText = body.dataset.bottomText || 'Frontend UI · React + Tailwind CSS inspired design';

    if (!body.querySelector('.accent-bar')) {
      const accent = document.createElement('div');
      accent.className = 'accent-bar';
      body.prepend(accent);
    }

    if (!body.querySelector('.navbar')) {
      const accent = body.querySelector('.accent-bar');
      accent.insertAdjacentHTML('afterend', buildNavbar(activeNav));
    }

    if (!body.querySelector('.bottom-strip')) {
      const bottomStrip = document.createElement('div');
      bottomStrip.className = 'bottom-strip';
      bottomStrip.textContent = bottomText;

      const firstScript = body.querySelector('script');
      if (firstScript) {
        firstScript.insertAdjacentElement('beforebegin', bottomStrip);
      } else {
        body.appendChild(bottomStrip);
      }
    }
  }

  document.addEventListener('DOMContentLoaded', function () {
    injectLayout();
    setupHamburger();
  });
})();

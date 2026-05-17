/**
 * Shared page chrome renderer.
 * Injects accent bar, navbar and bottom strip so pages reuse one structure.
 */
(function () {
  'use strict';

  function navLink(href, labelKey, activeKey, currentKey) {
    const isActive = activeKey === currentKey ? ' class="active"' : '';
    return `<li><a href="${href}"${isActive} data-i18n="${labelKey}">${getText(labelKey)}</a></li>`;
  }

  function getText(key) {
    return typeof i18n !== 'undefined' ? i18n.t(key) : key;
  }

  function buildLanguageSwitcher() {
    const locale = typeof i18n !== 'undefined' ? i18n.getLocale() : 'en';
    return `
      <li class="nav-language-item">
        <select class="nav-language-select" data-language-switcher data-i18n-aria-label="nav.languageLabel" aria-label="${getText('nav.languageLabel')}">
          <option value="en" data-i18n="languages.english" ${locale === 'en' ? 'selected' : ''}>${getText('languages.english')}</option>
          <option value="es" data-i18n="languages.spanish" ${locale === 'es' ? 'selected' : ''}>${getText('languages.spanish')}</option>
          <option value="ja" data-i18n="languages.japanese" ${locale === 'ja' ? 'selected' : ''}>${getText('languages.japanese')}</option>
        </select>
      </li>
    `;
  }

  function buildNavbar(activeKey) {
    const links = [
      navLink('index.html', 'nav.home', activeKey, 'home'),
      navLink('problems.html', 'nav.problems', activeKey, 'problems'),
      navLink('create.html', 'nav.create', activeKey, 'create'),
    ];

    const isAuthenticated = typeof auth !== 'undefined' ? auth.isAuthenticated() : localStorage.getItem('session_token') !== null;

    if (isAuthenticated) {
      const avatarActive = activeKey === 'profile' ? ' active' : '';
      links.push(
        `<li><a class="nav-avatar-link${avatarActive}" href="profile_page.html" data-i18n-aria-label="nav.profile" aria-label="${getText('nav.profile')}">` +
          `<img class="nav-avatar" src="https://i.pravatar.cc/96?img=12" data-i18n-alt="nav.profile" alt="${getText('nav.profile')}">` +
        '</a></li>'
      );
    } else {
      links.push(
        '<li><a class="nav-login-link" href="login.html" data-i18n="nav.login" aria-label="' + getText('nav.login') + '">' +
          getText('nav.login') +
        '</a></li>'
      );
    }

    links.push(buildLanguageSwitcher());

    return `
      <nav class="navbar">
        <a href="index.html" class="nav-logo">Realcode</a>
        <ul class="nav-links">${links.join('')}</ul>
        <button class="nav-hamburger" aria-label="${getText('nav.toggleNavigation')}" aria-expanded="false">
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

  function setupLanguageSwitcher() {
    const select = document.querySelector('[data-language-switcher]');
    if (!select || typeof i18n === 'undefined') {
      return;
    }

    select.value = i18n.getLocale();
    select.addEventListener('change', function () {
      i18n.setLocale(select.value);
      select.value = i18n.getLocale();
    });

    document.addEventListener('realcode:localechange', function () {
      select.value = i18n.getLocale();
    });
  }

  function injectLayout() {
    const body = document.body;
    if (!body) return;

    const activeNav = body.dataset.activeNav || '';
    const bottomText = body.dataset.bottomText || '';

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
    setupLanguageSwitcher();

    if (typeof i18n !== 'undefined') {
      i18n.applyPage();
    }
  });
})();

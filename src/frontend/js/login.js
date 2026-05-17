(function () {
  'use strict';

  const API_BASE = 'http://localhost:10000';

  let currentPanel = 'login';

  function getText(key, params) {
    return typeof i18n !== 'undefined' ? i18n.t(key, params) : key;
  }

  function showError(message) {
    const el = document.getElementById('error-message');
    el.textContent = message;
    el.style.display = 'block';
  }

  function clearError() {
    const el = document.getElementById('error-message');
    el.textContent = '';
    el.style.display = 'none';
  }

  function showPanel(panel) {
    currentPanel = panel;
    document.getElementById('login-panel').style.display = panel === 'login' ? 'block' : 'none';
    document.getElementById('register-panel').style.display = panel === 'register' ? 'block' : 'none';
    document.getElementById('form-title').textContent = panel === 'login' ? getText('login.signIn') : getText('login.createAccount');
    clearError();
  }

  function syncLocaleText() {
    document.getElementById('form-title').textContent = currentPanel === 'login' ? getText('login.signIn') : getText('login.createAccount');

    const loginBtn = document.getElementById('login-btn');
    const registerBtn = document.getElementById('register-btn');

    if (loginBtn && !loginBtn.disabled) {
      loginBtn.textContent = getText('login.signIn');
    }

    if (registerBtn && !registerBtn.disabled) {
      registerBtn.textContent = getText('login.createAccount');
    }
  }

  async function handleLogin(e) {
    e.preventDefault();
    clearError();

    const email    = document.getElementById('login-email').value.trim();
    const password = document.getElementById('login-password').value;

    if (!email || !password) {
      showError(getText('login.emailPasswordRequired'));
      return;
    }

    const btn = document.getElementById('login-btn');
    btn.disabled = true;
    btn.textContent = getText('login.signingIn');

    try {
      await auth.login(email, password);
      window.location.href = 'create.html';
    } catch (_err) {
      showError(getText('login.invalidEmailPassword'));
    } finally {
      btn.disabled = false;
      btn.textContent = getText('login.signIn');
    }
  }

  async function handleRegister(e) {
    e.preventDefault();
    clearError();

    const email    = document.getElementById('register-email').value.trim();
    const username = document.getElementById('register-username').value.trim();
    const password = document.getElementById('register-password').value;

    if (!email || !username || !password) {
      showError(getText('login.allFieldsRequired'));
      return;
    }

    const btn = document.getElementById('register-btn');
    btn.disabled = true;
    btn.textContent = getText('login.creatingAccount');

    try {
      const response = await fetch(`${API_BASE}/api/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, username, password }),
      });

      if (response.status === 409) {
        showError(getText('login.usernameOrEmailTaken'));
        return;
      }

      if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        showError(data.error || getText('login.registrationFailed'));
        return;
      }

      // Auto-login after successful registration
      await auth.login(email, password);
      window.location.href = 'create.html';
    } catch (_err) {
      showError(getText('login.couldNotReachServer'));
    } finally {
      btn.disabled = false;
      btn.textContent = getText('login.createAccount');
    }
  }

  document.addEventListener('DOMContentLoaded', function () {
    // Skip login page if already authenticated
    if (auth.isAuthenticated()) {
      window.location.href = 'create.html';
      return;
    }

    document.getElementById('login-form').addEventListener('submit', handleLogin);
    document.getElementById('register-form').addEventListener('submit', handleRegister);
    document.getElementById('show-register').addEventListener('click', function (e) {
      e.preventDefault();
      showPanel('register');
    });
    document.getElementById('show-login').addEventListener('click', function (e) {
      e.preventDefault();
      showPanel('login');
    });

    syncLocaleText();
    document.addEventListener('realcode:localechange', syncLocaleText);
  });
})();

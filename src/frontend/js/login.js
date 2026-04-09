(function () {
  'use strict';

  const API_BASE = 'http://localhost:10000';

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
    document.getElementById('login-panel').style.display = panel === 'login' ? 'block' : 'none';
    document.getElementById('register-panel').style.display = panel === 'register' ? 'block' : 'none';
    document.getElementById('form-title').textContent = panel === 'login' ? 'Sign In' : 'Create Account';
    clearError();
  }

  async function handleLogin(e) {
    e.preventDefault();
    clearError();

    const email    = document.getElementById('login-email').value.trim();
    const password = document.getElementById('login-password').value;

    if (!email || !password) {
      showError('Email and password are required.');
      return;
    }

    const btn = document.getElementById('login-btn');
    btn.disabled = true;
    btn.textContent = 'Signing in…';

    try {
      await auth.login(email, password);
      window.location.href = 'create.html';
    } catch (_err) {
      showError('Invalid email or password.');
    } finally {
      btn.disabled = false;
      btn.textContent = 'Sign In';
    }
  }

  async function handleRegister(e) {
    e.preventDefault();
    clearError();

    const email    = document.getElementById('register-email').value.trim();
    const username = document.getElementById('register-username').value.trim();
    const password = document.getElementById('register-password').value;

    if (!email || !username || !password) {
      showError('All fields are required.');
      return;
    }

    const btn = document.getElementById('register-btn');
    btn.disabled = true;
    btn.textContent = 'Creating account…';

    try {
      const response = await fetch(`${API_BASE}/api/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, username, password }),
      });

      if (response.status === 409) {
        showError('Username or email is already taken.');
        return;
      }

      if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        showError(data.error || 'Registration failed. Please try again.');
        return;
      }

      // Auto-login after successful registration
      await auth.login(email, password);
      window.location.href = 'create.html';
    } catch (_err) {
      showError('Could not reach the server. Is the backend running?');
    } finally {
      btn.disabled = false;
      btn.textContent = 'Create Account';
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
  });
})();

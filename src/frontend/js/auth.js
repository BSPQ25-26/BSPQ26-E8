/**
 * Authentication and Session Management
 * Handles user session tokens and authentication state
 */

class Auth {
    constructor() {
        this.TOKEN_KEY = 'session_token';
        this.REFRESH_TOKEN_KEY = 'refresh_token';
        this.USER_KEY = 'user_data';
        this.ACCESS_TOKEN_TTL_MS = 15 * 60 * 1000;
        this.refreshPromise = null;
    }

    /**
     * Save session token to localStorage
     */
    setToken(token) {
        if (token) {
            localStorage.setItem(this.TOKEN_KEY, token);
        }
    }

    /**
     * Save refresh token to localStorage
     */
    setRefreshToken(token) {
        if (token) {
            localStorage.setItem(this.REFRESH_TOKEN_KEY, token);
        }
    }

    /**
     * Save both session tokens to localStorage
     */
    setTokens(accessToken, refreshToken) {
        if (accessToken) {
            this.setToken(accessToken);
        } else {
            localStorage.removeItem(this.TOKEN_KEY);
        }

        if (refreshToken) {
            this.setRefreshToken(refreshToken);
        } else {
            localStorage.removeItem(this.REFRESH_TOKEN_KEY);
        }
    }

    /**
     * Get current session token
     */
    getToken() {
        return localStorage.getItem(this.TOKEN_KEY);
    }

    /**
     * Get current refresh token
     */
    getRefreshToken() {
        return localStorage.getItem(this.REFRESH_TOKEN_KEY);
    }

    /**
     * Remove session token (logout)
     */
    clearToken() {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.REFRESH_TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);
    }

    /**
     * Check if user is authenticated
     */
    isAuthenticated() {
        return this.getToken() !== null || this.getRefreshToken() !== null;
    }

    /**
     * Check whether the custom access token payload has passed its TTL.
     */
    isAccessTokenExpired(token = this.getToken()) {
        if (!token || typeof token !== 'string') {
            return false;
        }

        const parts = token.split('.');
        if (parts.length !== 2) {
            return false;
        }

        try {
            const payload = this.decodeBase64Url(parts[0]);
            const payloadParts = payload.split(':');
            if (payloadParts.length !== 2) {
                return false;
            }

            const issuedAtSeconds = Number(payloadParts[1]);
            if (!Number.isFinite(issuedAtSeconds)) {
                return false;
            }

            return Date.now() >= (issuedAtSeconds * 1000) + this.ACCESS_TOKEN_TTL_MS;
        } catch (_error) {
            return false;
        }
    }

    decodeBase64Url(value) {
        const base64 = value.replace(/-/g, '+').replace(/_/g, '/');
        const padded = base64.padEnd(base64.length + ((4 - base64.length % 4) % 4), '=');
        return atob(padded);
    }

    /**
     * Return a usable access token, refreshing it first when it is expired.
     */
    async getValidToken() {
        const token = this.getToken();
        if (!token || !this.isAccessTokenExpired(token)) {
            return token;
        }

        return this.refreshAccessToken();
    }

    /**
     * Refresh access and refresh tokens using the active refresh token.
     */
    async refreshAccessToken() {
        if (this.refreshPromise) {
            return this.refreshPromise;
        }

        this.refreshPromise = this.doRefreshAccessToken();
        try {
            return await this.refreshPromise;
        } finally {
            this.refreshPromise = null;
        }
    }

    async doRefreshAccessToken() {
        const refreshToken = this.getRefreshToken();
        if (!refreshToken) {
            return null;
        }

        try {
            const API_BASE = window.API_BASE || 'http://localhost:10000';
            const response = await fetch(`${API_BASE}/api/auth/refresh`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ refreshToken })
            });

            if (!response.ok) {
                this.clearToken();
                return null;
            }

            const data = await response.json();
            if (!data.accessToken || !data.refreshToken) {
                this.clearToken();
                return null;
            }

            this.setTokens(data.accessToken, data.refreshToken);
            if (data.user) {
                this.setUser(data.user);
            }

            return data.accessToken;
        } catch (error) {
            console.error('Token refresh error:', error);
            this.clearToken();
            return null;
        }
    }

    /**
     * Save user data
     */
    setUser(userData) {
        if (userData) {
            localStorage.setItem(this.USER_KEY, JSON.stringify(userData));
        }
    }

    /**
     * Get current user data
     */
    getUser() {
        const userData = localStorage.getItem(this.USER_KEY);
        return userData ? JSON.parse(userData) : null;
    }

    /**
     * Login user with credentials
     * @param {string} email
     * @param {string} password
     * @returns {Promise<Object>} User data and token
     */
    async login(email, password) {
        try {
            const API_BASE = window.API_BASE || 'http://localhost:10000';
            const response = await fetch(`${API_BASE}/api/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ email, password })
            });

            if (!response.ok) {
                throw new Error('Login failed');
            }

            const data = await response.json();

            // Save token and user data
            if (data.accessToken) {
                this.setTokens(data.accessToken, data.refreshToken);
            }
            if (data.user) {
                this.setUser(data.user);
            }

            return data;
        } catch (error) {
            console.error('Login error:', error);
            throw error;
        }
    }

    /**
     * Logout current user
     */
    logout() {
        this.clearToken();
        // Redirect to login or home page
        window.location.href = 'index.html';
    }

    /**
     * Redirect to login if not authenticated
     */
    requireAuth() {
        if (!this.isAuthenticated()) {
            window.location.href = 'login.html';
        }
    }
}

// Export singleton instance
const auth = new Auth();

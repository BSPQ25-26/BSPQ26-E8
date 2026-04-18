/**
 * Authentication and Session Management
 * Handles user session tokens and authentication state
 */

class Auth {
    constructor() {
        this.TOKEN_KEY = 'session_token';
        this.USER_KEY = 'user_data';
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
     * Get current session token
     */
    getToken() {
        return localStorage.getItem(this.TOKEN_KEY);
    }

    /**
     * Remove session token (logout)
     */
    clearToken() {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);
    }

    /**
     * Check if user is authenticated
     */
    isAuthenticated() {
        return this.getToken() !== null;
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
                this.setToken(data.accessToken);
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

/**
 * API Client
 * Handles all HTTP requests to the backend with automatic token injection
 */

class ApiClient {
    constructor() {
        const apiBase = window.API_BASE || 'http://localhost:10000';
        this.baseURL = `${apiBase.replace(/\/$/, '')}/api`;
    }

    isAuthEndpoint(endpoint) {
        return typeof endpoint === 'string' && endpoint.startsWith('/auth/');
    }

    /**
     * Get authorization headers with token
     */
    getHeaders(customHeaders = {}) {
        const headers = {
            'Content-Type': 'application/json',
            ...customHeaders
        };

        const token = typeof auth !== 'undefined' ? auth.getToken() : null;
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        return headers;
    }

    /**
     * Generic fetch wrapper
     */
    async request(endpoint, options = {}, shouldRetryAuth = true) {
        if (!this.isAuthEndpoint(endpoint) && typeof auth !== 'undefined') {
            await auth.getValidToken();
        }

        const url = `${this.baseURL}${endpoint}`;

        const config = {
            ...options,
            headers: this.getHeaders(options.headers)
        };

        try {
            const response = await fetch(url, config);

            // Handle 401 Unauthorized - token expired or invalid
            if (response.status === 401) {
                if (shouldRetryAuth && !this.isAuthEndpoint(endpoint) && typeof auth !== 'undefined') {
                    const refreshedToken = await auth.refreshAccessToken();
                    if (refreshedToken) {
                        return this.request(endpoint, options, false);
                    }
                }

                if (typeof auth !== 'undefined') {
                    auth.clearToken();
                }
                window.location.href = 'login.html';
                throw new Error('Unauthorized - Please login again');
            }

            if (!response.ok) {
                const errorBody = await this.parseResponse(response);
                const message = errorBody && errorBody.error
                    ? errorBody.error
                    : `HTTP ${response.status}: ${response.statusText}`;
                const error = new Error(message);
                error.status = response.status;
                error.data = errorBody;
                throw error;
            }

            return this.parseResponse(response);

        } catch (error) {
            console.error('API Request failed:', error);
            throw error;
        }
    }

    /**
     * Parse JSON responses while keeping no-content responses as null.
     */
    async parseResponse(response) {
        const text = await response.text();
        if (!text) {
            return null;
        }

        try {
            return JSON.parse(text);
        } catch (_error) {
            return text;
        }
    }

    /**
     * GET request
     */
    async get(endpoint) {
        return this.request(endpoint, {
            method: 'GET'
        });
    }

    /**
     * POST request
     */
    async post(endpoint, data) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    }

    /**
     * PUT request
     */
    async put(endpoint, data) {
        return this.request(endpoint, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    }

    /**
     * DELETE request
     */
    async delete(endpoint) {
        return this.request(endpoint, {
            method: 'DELETE'
        });
    }

    // ===== Specific API endpoints =====

    /**
     * Get all problems
     */
    async getProblems(filters = {}) {
        const params = new URLSearchParams(filters);
        return this.get(`/problems?${params}`);
    }

    /**
     * Get problem by ID
     */
    async getProblem(id) {
        return this.get(`/problems/${id}`);
    }

    /**
     * Create new problem
     */
    async createProblem(problemData) {
        return this.post('/problems', problemData);
    }

    /**
     * Get workspace data for a problem
     */
    async getWorkspace(problemId) {
        return this.get(`/workspace/${problemId}`);
    }

    /**
     * Submit solution
     */
    async submitSolution(problemId, code, language) {
        return this.post(`/workspace/${problemId}/submit`, {
            code,
            language
        });
    }

    /**
     * Run code against test cases
     */
    async runCode(problemId, code, language) {
        return this.post(`/workspace/${problemId}/run`, {
            code,
            language
        });
    }
}

// Export singleton instance
const api = new ApiClient();

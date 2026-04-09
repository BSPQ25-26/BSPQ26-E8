/**
 * API Client
 * Handles all HTTP requests to the backend with automatic token injection
 */

class ApiClient {
    constructor() {
        this.baseURL = 'http://localhost:10000/api';
    }

    /**
     * Get authorization headers with token
     */
    getHeaders(customHeaders = {}) {
        const headers = {
            'Content-Type': 'application/json',
            ...customHeaders
        };

        const token = auth.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        return headers;
    }

    /**
     * Generic fetch wrapper
     */
    async request(endpoint, options = {}) {
        const url = `${this.baseURL}${endpoint}`;

        const config = {
            ...options,
            headers: this.getHeaders(options.headers)
        };

        try {
            const response = await fetch(url, config);

            // Handle 401 Unauthorized - token expired or invalid
            if (response.status === 401) {
                auth.clearToken();
                window.location.href = 'login.html';
                throw new Error('Unauthorized - Please login again');
            }

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            // Handle empty responses
            const text = await response.text();
            return text ? JSON.parse(text) : null;

        } catch (error) {
            console.error('API Request failed:', error);
            throw error;
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

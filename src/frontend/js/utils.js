/**
 * Utility Functions
 * Common helper functions used across the application
 */

const utils = {
    /**
     * Show loading indicator
     */
    showLoading(elementId) {
        const element = document.getElementById(elementId);
        if (element) {
            element.innerHTML = `<p>${typeof i18n !== 'undefined' ? i18n.t('common.loading') : 'Loading...'}</p>`;
        }
    },

    /**
     * Show error message
     */
    showError(elementId, message) {
        const element = document.getElementById(elementId);
        if (element) {
            element.innerHTML = `<p class="error">${message}</p>`;
        }
    },

    /**
     * Format date to readable string
     */
    formatDate(dateString) {
        const date = new Date(dateString);
        const locale = typeof i18n !== 'undefined' ? i18n.getLocale() : 'en';
        return date.toLocaleDateString(locale, {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    },

    /**
     * Debounce function calls
     */
    debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    },

    /**
     * Get URL parameter
     */
    getUrlParameter(name) {
        const params = new URLSearchParams(window.location.search);
        return params.get(name);
    },

    /**
     * Redirect to page
     */
    redirectTo(page) {
        window.location.href = page;
    }
};

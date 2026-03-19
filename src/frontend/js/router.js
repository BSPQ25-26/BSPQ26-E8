// Simple Hash Router for SPA navigation

class Router {
    constructor() {
        this.routes = {};
        this.currentRoute = null;
    }

    addRoute(path, handler) {
        this.routes[path] = handler;
    }

    navigate(path) {
        window.location.hash = path;
    }

    // Parse current hash and extract route + params
    parseLocation() {
        const hash = window.location.hash.slice(1) || '/';

        // Try exact match first
        if (this.routes[hash]) {
            return { route: hash, params: {} };
        }

        // Try dynamic routes (e.g., /workspace/:id)
        for (const route in this.routes) {
            const routeRegex = new RegExp('^' + route.replace(/:\w+/g, '([^/]+)') + '$');
            const match = hash.match(routeRegex);

            if (match) {
                const paramNames = route.match(/:\w+/g) || [];
                const params = {};

                paramNames.forEach((param, index) => {
                    params[param.slice(1)] = match[index + 1];
                });

                return { route, params };
            }
        }

        return { route: '/', params: {} };
    }

    render() {
        const { route, params } = this.parseLocation();
        const handler = this.routes[route];

        if (handler) {
            this.currentRoute = route;
            const content = handler(params);
            const appContainer = document.getElementById('app');

            if (appContainer) {
                appContainer.innerHTML = content;
            }
        }
    }

    init() {
        window.addEventListener('hashchange', () => this.render());
        this.render();
    }
}

// Export singleton instance
const router = new Router();
export default router;

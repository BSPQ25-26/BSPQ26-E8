//Main Application Entry Point
//Initializes the router and registers all routes

import router from './router.js';
import HomeView from './views/home.js';
import ProblemCreateView from './views/problem-create.js';
import WorkspaceView from './views/workspace.js';

//Initialize the application
function initApp() {
    // Register all routes
    router.addRoute('/', HomeView);
    router.addRoute('/problems/create', ProblemCreateView);
    router.addRoute('/workspace/:id', WorkspaceView);

    // Initialize router
    router.init();

    console.log('LeetCode Clone App initialized');
}

// Wait for DOM to be ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initApp);
} else {
    initApp();
}

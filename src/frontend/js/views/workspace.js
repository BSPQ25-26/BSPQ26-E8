//Workspace View - Coding Editor

export default function WorkspaceView(params) {
    const problemId = params.id || '1';

    return `
        <div class="view-container">
            <h1>Workspace Page</h1>
            <p>Coding workspace for Problem ID: <strong>${problemId}</strong></p>
            <p>Editor and problem description will go here...</p>
            <button onclick="window.location.hash = '/'">Back to Home</button>
        </div>
    `;
}

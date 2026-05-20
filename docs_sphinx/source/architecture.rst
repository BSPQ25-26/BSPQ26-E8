Architecture
============

System Design
-------------

BSPQ26-E8 follows a **layered, service-oriented architecture**:

.. code-block:: text

   ┌─────────────────────────────────────────────┐
   │          Frontend (Nginx/HTML/JS)           │
   │          (Port 3000)                        │
   └─────────┬───────────────────────────────────┘
             │
   ┌─────────▼───────────────────────────────────┐
   │       Spring Boot REST API                  │
   │       (Port 10000)                          │
   │  ┌─────────────────────────────────────┐   │
   │  │  Controllers (/api/*)               │   │
   │  ├─────────────────────────────────────┤   │
   │  │  Services (Business Logic)          │   │
   │  ├─────────────────────────────────────┤   │
   │  │  Repositories (Spring Data JPA)     │   │
   │  └─────────────────────────────────────┘   │
   └─────────┬───────────────┬───────────────────┘
             │               │
   ┌─────────▼──┐    ┌──────▼──────────────────┐
   │ PostgreSQL │    │   Judge0 Service        │
   │ (Database) │    │   (Code Execution)      │
   └────────────┘    │   - Server (2358)       │
                     │   - Database            │
                     │   - Redis Cache         │
                     │   - Worker Processes    │
                     └─────────────────────────┘

Backend Core Modules
--------------------

**auth** (Authentication & Session Management)
   * ``POST /api/auth/register`` - User registration
   * ``POST /api/auth/login`` - User login with JWT
   * ``POST /api/auth/token/refresh`` - Token refresh
   * ``POST /api/auth/logout`` - Logout

**user** (User Profiles)
   * ``GET /api/users/{id}`` - Retrieve user profile
   * ``GET /api/users`` - List all users
   * User information queries and management

**problem** (Coding Problems)
   * ``POST /api/problems`` - Create problem
   * ``GET /api/problems`` - List all problems
   * ``GET /api/problems/{id}`` - Get problem details
   * ``PUT /api/problems/{id}`` - Update problem
   * ``DELETE /api/problems/{id}`` - Delete problem

**submission** (Code Submissions)
   * ``POST /api/submissions`` - Create submission
   * ``GET /api/submissions`` - List user submissions
   * ``GET /api/submissions/{id}`` - Get submission details
   * ``PUT /api/submissions/{id}/resubmit`` - Resubmit code
   * Async result tracking and polling

**codeexecution** (Judge0 Integration)
   * Submits code to Judge0 for compilation and execution
   * Polls for execution status and results
   * Handles language-specific configurations
   * Manages timeouts and resource limits

**evaluator** (Result Processing)
   * Processes Judge0 responses
   * Determines submission success/failure
   * Stores evaluation results in database

**workspace** (User Workspaces)
   * User-specific problem and submission isolation
   * Workspace management and queries

**common** (Shared Infrastructure)
   * ``WebConfig`` - CORS configuration for ``/api/**`` paths
   * ``HealthController`` - ``GET /api/health`` health check endpoint
   * Exception handling and global error responses
   * Shared utilities and constants

Request Flow Example: Code Submission
--------------------------------------

.. code-block:: text

   1. Client (Frontend)
      POST /api/submissions with code, problemId, languageId
      │
   2. Spring Controller (SubmissionController)
      Validates JWT token, extracts userId
      │
   3. Service Layer (SubmissionService)
      ├─ Creates Submission entity in database
      ├─ Calls CodeExecutionService to queue execution
      └─ Returns submission ID
      │
   4. CodeExecutionService
      Sends code to Judge0 API (external)
      │
   5. Judge0 (External Service)
      Compiles and executes code
      Returns execution status, stdout, stderr, runtime
      │
   6. PollingController (Frontend)
      GET /api/submissions/{id}
      │
   7. EvaluatorService
      Processes Judge0 results
      Updates Submission with final status
      │
   8. Client receives final result

Database Schema
---------------

Core entities managed by Flyway migrations in ``src/backend/src/main/resources/db/migration/``:

* **User** - User accounts, credentials, profiles
* **Problem** - Coding problems with descriptions, test cases, constraints
* **Submission** - Code submissions with language, status, results
* **ExecutionResult** - Judge0 response data, stdout, stderr
* And supporting entities for workspace, relationships, etc.

External Service Integration
-----------------------------

**Judge0 Services** (docker-compose defines these):

* ``judge0-db`` (PostgreSQL 16.2) - Judge0 database
* ``judge0-server`` (Port 2358) - Judge0 API endpoint
* ``judge0-redis`` (Redis 7.2.4) - Result caching
* ``judge0-worker`` - Background execution workers

The backend communicates with Judge0 via HTTP REST API at ``https://ce.judge0.com`` (or local ``http://judge0-server:2358`` in development).

Deployment Services
-------------------

In ``docker-compose.yml``:

* ``postgres`` - Main application database (PostgreSQL 16-alpine)
* ``backend`` - Spring Boot application container
* ``frontend`` - Nginx serving static assets
* ``performance`` - k6 performance test runner
* ``judge0-*`` - Judge0 infrastructure services

Configuration & Environment
----------------------------

Backend configuration (``application.yml``) is environment-aware:

* Database connection: ``POSTGRES_DB``, ``POSTGRES_USER``, ``POSTGRES_PASSWORD``
* Server port: ``server.port`` (default 10000)
* JWT secret: ``app.security.jwt.secret``
* Judge0 endpoint: ``JUDGE0_BASE_URL`` and ``JUDGE0_ENABLED``
* Queue settings: ``CODE_EXECUTION_QUEUE_ENABLED``

All sensitive values are read from ``.env`` file at runtime.

Security Considerations
------------------------

* **Authentication:** JWT bearer tokens in ``Authorization: Bearer <token>`` headers
* **CORS:** Configured in ``WebConfig`` for frontend domain
* **Password Hashing:** Spring Security with bcrypt
* **Database:** Credentials injected at runtime, never hardcoded
* **Judge0 Isolation:** Sandboxed execution prevents malicious code impact

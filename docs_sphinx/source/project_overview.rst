Project Overview
================

Purpose
-------

BSPQ26-E8 is a competitive programming platform where:

* Users register and authenticate
* Problem creators publish coding challenges
* Users submit code for automated evaluation
* Results are tracked and displayed in real-time

Technology Stack
----------------

Backend
~~~~~~~

* **Java 21** with Spring Boot 3.3.5
* **Spring Data JPA** for database abstraction
* **Flyway** for schema migrations
* **PostgreSQL 16** for persistent storage
* **Springdoc OpenAPI 2.5.0** for API documentation
* **Testcontainers** for integration tests
* **ArchUnit** for architecture testing
* **Mockito** for unit test mocking
* **REST Assured** for API testing

Frontend
~~~~~~~~

* **HTML/CSS/JavaScript** for UI
* **Nginx** for static hosting in Docker
* **Playwright** for end-to-end testing across desktop, tablet, and mobile viewports
* **npm** for dependency management

Infrastructure & DevOps
~~~~~~~~~~~~~~~~~~~~~~~

* **Docker & Docker Compose** for containerization
* **PostgreSQL 16-alpine** for main application database
* **Judge0** with Redis caching for code execution
* **k6** for performance testing
* **GitHub Actions** for CI/CD pipelines

Repository Structure
--------------------

.. code-block:: text

   BSPQ26-E8/
   ├─ src/
   │  ├─ backend/                           # Spring Boot application
   │  │  ├─ src/main/java/.../backend/
   │  │  │  ├─ auth/                        # Authentication & user login
   │  │  │  ├─ user/                        # User profile management
   │  │  │  ├─ problem/                     # Problem CRUD operations
   │  │  │  ├─ submission/                  # Submission tracking
   │  │  │  ├─ codeexecution/               # Judge0 integration
   │  │  │  ├─ evaluator/                   # Result evaluation logic
   │  │  │  ├─ workspace/                   # User workspaces
   │  │  │  └─ common/                      # Shared utilities & configuration
   │  │  └─ src/main/resources/
   │  │     ├─ application.yml              # Application configuration
   │  │     └─ db/migration/                # Flyway database migrations
   │  └─ frontend/                          # Static HTML/CSS/JS
   │     ├─ pages/                          # HTML pages
   │     ├─ js/                             # JavaScript logic
   │     ├─ css/                            # Stylesheets
   │     ├─ tests/                          # Playwright E2E tests
   │     └─ nginx.conf                      # Nginx configuration
   ├─ docs/                                 # Jekyll documentation (legacy)
   ├─ docs_sphinx/                          # This Sphinx documentation
   ├─ docker-compose.yml                    # Service orchestration
   ├─ judge0.conf                           # Judge0 configuration
   └─ .github/workflows/                    # CI/CD workflows

Core Features
-------------

* **User Authentication:** Secure registration, login, token refresh, and logout
* **Problem Management:** Create, list, update, and delete coding problems
* **Code Submission:** Submit code in supported languages with automatic evaluation
* **Real-time Results:** Async evaluation with status tracking via Judge0
* **Performance Monitoring:** k6 performance tests integrated into CI
* **API Documentation:** Auto-generated Swagger UI for all endpoints

Quality Assurance
-----------------

* **Unit Tests:** Executed in CI with JUnit 5
* **Integration Tests:** Testcontainers with real PostgreSQL instances
* **Coverage Gates:** JaCoCo enforces minimum coverage thresholds
* **E2E Tests:** Playwright tests across multiple device types
* **Performance Tests:** k6 scripts for load testing critical endpoints
* **Architecture Tests:** ArchUnit validates code structure and constraints

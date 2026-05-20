BSPQ26-E8 Documentation
=======================

**BSPQ26-E8** is a code execution platform for competitive programming. Users can:

* Register and authenticate
* Create and manage coding problems
* Submit source code for evaluation
* Track submission status and results in real-time

The platform integrates Judge0 for reliable code execution across multiple programming languages.

Technology Stack
----------------

* **Backend:** Java 21, Spring Boot 3.3.x, Spring Data JPA, PostgreSQL
* **Frontend:** HTML, CSS, JavaScript, Nginx
* **Code Execution:** Judge0 with Redis caching
* **Infrastructure:** Docker, Docker Compose, GitHub Actions
* **Testing:** JUnit 5, Testcontainers, Playwright, k6 performance testing

Navigation Guide
----------------

* Start with :doc:`project_overview` for full scope and architecture overview.
* Explore :doc:`architecture` to understand core modules and request flows.
* Use :doc:`api_reference` for backend API endpoints.
* Follow :doc:`local_setup` to get the platform running locally.
* Check :doc:`testing_ci` for testing commands and CI/CD workflows.
* Review :doc:`sprint3_review` for GitHub Actions CI/CD setup details.

.. toctree::
   :maxdepth: 2
   :caption: Contents

   project_overview
   architecture
   api_reference
   local_setup
   testing_ci
   sprint3_review


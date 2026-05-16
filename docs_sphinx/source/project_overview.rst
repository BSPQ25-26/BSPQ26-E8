Project Overview
================

Goal
----

BSPQ26-E8 is a platform where users can:

* Register and authenticate
* Create and browse programming problems
* Submit source code in supported languages
* Track submission status and results

Scope for Sprint 3
------------------

The documented scope covers:

* Authentication and user management endpoints
* Problem creation, update, listing, and deletion
* Submission creation, tracking, and resubmission flow
* CI and quality gates for backend and frontend

Technology Stack
----------------

Backend
~~~~~~~

* Java 21
* Spring Boot 3.3.x
* Spring Data JPA
* Flyway migrations
* PostgreSQL
* Springdoc OpenAPI

Frontend
~~~~~~~~

* HTML/CSS/JavaScript
* Nginx container for static hosting
* Playwright tests

Infrastructure
~~~~~~~~~~~~~~

* Docker and Docker Compose
* Judge0 services for code execution
* GitHub Actions CI workflows

Quality and Engineering Practices
---------------------------------

* Unit and integration tests are executed in CI
* JaCoCo coverage verification is enforced in backend checks
* Flyway migrations provide controlled database evolution
* API contracts are discoverable through Swagger/OpenAPI

Repository Structure
--------------------

.. code-block:: text

   BSPQ26-E8/
   ├─ src/
   │  ├─ backend/
   │  │  ├─ src/main/java/com/bspq26e8/backend/
   │  │  └─ src/main/resources/
   │  └─ frontend/
   ├─ docs/               (existing Jekyll docs)
   └─ docs_sphinx/        (this Sphinx documentation)

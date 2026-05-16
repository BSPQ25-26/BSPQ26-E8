Architecture
============

High-Level View
---------------

The system follows a layered architecture around a REST API:

* **Controllers** expose HTTP endpoints under ``/api/**``
* **Services** implement business logic
* **Repositories** provide data access via Spring Data JPA
* **Entities** map domain concepts to PostgreSQL tables

Request Flow
------------

.. code-block:: text

   Client -> Controller -> Service -> Repository -> PostgreSQL
                          |
                          +-> CodeExecutionService -> Judge0

Main Backend Modules
--------------------

* ``auth``: registration, login, token refresh, logout
* ``user``: user profile queries
* ``problem``: CRUD operations for coding problems
* ``submission``: submission lifecycle and result tracking
* ``codeexecution``: Judge0-oriented execution workflow
* ``common``: shared web configuration and health endpoint

Runtime Components
------------------

In ``docker-compose.yml`` the following services are defined:

* ``postgres`` for the main application database
* ``backend`` (Spring Boot on port 10000)
* ``frontend`` (Nginx on port 3000)
* ``performance`` (k6 runner)
* ``judge0-db``, ``judge0-server``, ``judge0-redis``, ``judge0-worker``

Configuration
-------------

The backend uses ``application.yml`` with environment-variable overrides for:

* Database URL, username, password
* Server port (default ``10000``)
* Access-token secret

Flyway is enabled for schema and seed migrations located in:

.. code-block:: text

   src/backend/src/main/resources/db/migration

Design Notes
------------

* CORS is centralized in ``common/WebConfig`` for ``/api/**`` paths.
* Health checks are exposed through ``/api/health``.
* Authenticated operations use access-token extraction in controllers/services.

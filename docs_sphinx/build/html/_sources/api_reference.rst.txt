API Reference (High Level)
==========================

Endpoint Groups
---------------

.. list-table::
   :header-rows: 1

   * - Group
     - Base Path
     - Purpose
   * - Authentication
     - ``/api/auth``
     - Login lifecycle and token management
   * - Users
     - ``/api/users``
     - User profile lookup operations
   * - Problems
     - ``/api/problems``
     - Problem authoring and public catalog access
   * - Submissions
     - ``/api/submissions``
     - Code submission lifecycle and progress tracking
   * - Health
     - ``/api/health``
     - Service availability check

Authentication
--------------

Base path: ``/api/auth``

* ``POST /register`` - create a new user
* ``POST /login`` - authenticate and obtain access/refresh tokens
* ``POST /refresh`` - refresh token pair
* ``POST /logout`` - invalidate refresh token

Users
-----

Base path: ``/api/users``

* ``GET /{id}`` - fetch user by UUID
* ``GET /by-email?email=...`` - fetch user by email

Problems
--------

Base path: ``/api/problems``

* ``POST /`` - create problem (authenticated)
* ``GET /`` - list public problems with filters
* ``GET /{problemId}`` - get problem detail
* ``GET /by-author/{authorId}`` - list author problems
* ``PUT /{problemId}`` - update problem by author
* ``DELETE /{problemId}`` - delete problem by author

Submissions
-----------

Base path: ``/api/submissions``

* ``POST /`` - create submission
* ``GET /mine`` - list authenticated user submissions
* ``GET /mine/latest`` - latest submission
* ``GET /problem/{problemId}`` - submissions for one problem
* ``GET /problem/{problemId}/best`` - best user submission for problem
* ``PUT /{submissionId}`` - update submission
* ``POST /{submissionId}/resubmit`` - create new submission from previous one
* ``DELETE /{submissionId}`` - delete submission

Health
------

* ``GET /api/health`` - returns ``{"status": "ok"}``

OpenAPI/Swagger
---------------

Springdoc is enabled in backend dependencies.

* Swagger UI: ``http://localhost:10000/swagger-ui/index.html``
* OpenAPI JSON: ``http://localhost:10000/v3/api-docs``

Common Response Patterns
------------------------

* Success responses use standard HTTP status codes (200, 201, 204).
* Validation errors return ``400`` with an ``error`` message payload.
* Authentication failures return ``401``.
* Missing resources return ``404``.

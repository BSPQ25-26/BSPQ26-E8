Local Setup
===========

Prerequisites
-------------

* **Java 21** (for backend compilation and running)
* **Docker Desktop** (with Docker Compose support)
* **Python 3.8+** (only required for building Sphinx documentation)
* **Node.js 18+** (for frontend dependency management and Playwright)
* **Git** (for repository cloning)

Quick Start: Full Stack with Docker Compose
--------------------------------------------

The easiest way to run the entire platform locally:

.. code-block:: powershell

   cd C:\Deusto\BSPQ26-E8\BSPQ26-E8
   docker-compose up --build

This starts all services:

* **Frontend:** http://localhost:3000
* **Backend API:** http://localhost:10000
* **Swagger UI:** http://localhost:10000/swagger-ui/index.html
* **Health Check:** http://localhost:10000/api/health
* **Judge0 API:** http://localhost:2358

To stop all services:

.. code-block:: powershell

   docker-compose down

To remove all data and rebuild from scratch:

.. code-block:: powershell

   docker-compose down -v
   docker-compose up --build

Option: Run Backend Standalone with Gradle
-------------------------------------------

If you only need the backend API (with external Judge0):

.. code-block:: powershell

   cd src\backend
   .\gradlew.bat bootRun

The backend starts on ``http://localhost:10000`` and connects to:

* PostgreSQL on ``localhost:5432`` (requires running separately)
* Judge0 on ``https://ce.judge0.com`` (public instance)

To run with local Judge0 instead, set in ``.env``:

.. code-block:: text

   JUDGE0_BASE_URL=http://localhost:2358

Option: Run Frontend Standalone with Nginx
-------------------------------------------

Build and run just the frontend container:

.. code-block:: powershell

   cd src\frontend
   docker build -t bspq26e8-frontend .
   docker run --rm -p 3000:80 bspq26e8-frontend

The frontend runs on ``http://localhost:3000`` but requires a backend API at ``http://localhost:10000``.

Environment Variables
---------------------

Create a ``.env`` file in the repository root with:

.. code-block:: text

   # PostgreSQL
   POSTGRES_DB=realcode
   POSTGRES_USER=postgres
   POSTGRES_PASSWORD=your_secure_password

   # Backend
   JUDGE0_BASE_URL=https://ce.judge0.com
   JUDGE0_ENABLED=true
   CODE_EXECUTION_QUEUE_ENABLED=true

   # Optional: JWT secret for auth tokens
   APP_SECURITY_JWT_SECRET=your_jwt_secret_key_here

All services read these values from ``.env`` at startup via Docker Compose.

Building Sphinx Documentation
------------------------------

From the repository root, build the HTML documentation:

.. code-block:: powershell

   python -m sphinx -b html .\docs_sphinx\source .\docs_sphinx\build\html

The output HTML entry point:

.. code-block:: text

   docs_sphinx\build\html\index.html

For live preview while editing (auto-rebuilds on file changes):

.. code-block:: powershell

   pip install sphinx-autobuild
   python -m sphinx_autobuild .\docs_sphinx\source .\docs_sphinx\build\html

Then open ``http://localhost:8000`` in your browser. The documentation rebuilds automatically when you edit .rst files.

Common Development Workflows
-----------------------------

**Backend Development**

.. code-block:: powershell

   cd src\backend
   .\gradlew.bat bootRun

**Frontend Development**

.. code-block:: powershell

   cd src\frontend
   docker build -t bspq26e8-frontend .
   docker run --rm -p 3000:80 bspq26e8-frontend

**Run Full Stack (Recommended for end-to-end testing)**

.. code-block:: powershell

   docker-compose up --build

**Check Backend Health**

.. code-block:: powershell

   curl http://localhost:10000/api/health

**View API Documentation**

Visit ``http://localhost:10000/swagger-ui/index.html`` in your browser.

**View Judge0 Status**

Visit ``http://localhost:2358`` (if running locally) or check ``https://ce.judge0.com`` (if using public instance).

Troubleshooting
---------------

**Port Already in Use**
   Stop any running containers and check for lingering processes:

   .. code-block:: powershell

      docker-compose down
      netstat -ano | findstr :3000
      taskkill /PID <PID> /F

**Backend Fails to Start**
   Verify database connectivity:

   .. code-block:: powershell

      docker-compose logs postgres
      docker-compose logs backend

**Frontend Cannot Connect to Backend**
   Ensure backend is running and accessible:

   .. code-block:: powershell

      curl http://localhost:10000/api/health

**Gradle Build Failures**
   Clear Gradle cache:

   .. code-block:: powershell

      cd src\backend
      .\gradlew.bat clean build

**Judge0 Not Responding**
   If using local Judge0, verify all Judge0 services are healthy:

   .. code-block:: powershell

      docker-compose logs judge0-server
      docker-compose logs judge0-worker

   If using public Judge0, verify your internet connection and check ``https://ce.judge0.com/health``.

**Python/Sphinx Not Found**
   Install or update Python:

   .. code-block:: powershell

      pip install sphinx sphinx-autobuild
      python --version

**Docker Image Build Failures**
   Ensure Docker daemon is running and try building with verbose output:

   .. code-block:: powershell

      docker-compose build --verbose

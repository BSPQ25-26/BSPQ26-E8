Local Setup
===========

Prerequisites
-------------

* Java 21
* Docker Desktop
* Python 3 (for Sphinx only)

Option 1: Run Full Stack with Docker Compose
--------------------------------------------

From the repository root:

.. code-block:: powershell

   docker-compose up --build

Main local URLs:

* Frontend: ``http://localhost:3000``
* Backend API: ``http://localhost:10000``
* Health: ``http://localhost:10000/api/health``
* Swagger UI: ``http://localhost:10000/swagger-ui/index.html``
* Judge0 API: ``http://localhost:2358``

To stop:

.. code-block:: powershell

   docker-compose down

Option 2: Run Backend with Gradle
---------------------------------

From ``src\backend``:

.. code-block:: powershell

   .\gradlew.bat bootRun

Option 3: Run Frontend with Nginx Container
-------------------------------------------

From ``src\frontend``:

.. code-block:: powershell

   docker build -t bspq26e8-frontend .
   docker run --rm -p 3000:80 bspq26e8-frontend

Environment Variables
---------------------

The dockerized setup reads values from ``.env`` at repository root for:

* ``POSTGRES_DB``
* ``POSTGRES_USER``
* ``POSTGRES_PASSWORD``
* optional backend security settings such as access-token secret

Sphinx Build Commands
---------------------

From repository root:

.. code-block:: powershell

   python -m sphinx -b html .\docs_sphinx\source .\docs_sphinx\build\html

Output entry point:

.. code-block:: text

   docs_sphinx\build\html\index.html

Recommended live preview while editing:

.. code-block:: powershell

   python -m sphinx_autobuild .\docs_sphinx\source .\docs_sphinx\build\html

Troubleshooting
---------------

* If ``sphinx-build`` is not found, use ``python -m sphinx ...`` instead of ``make.bat``.
* If ports are already in use, stop previous containers before starting Compose again.
* If backend startup fails, verify database credentials in ``.env`` and container health.

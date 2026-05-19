Testing and CI
==============

Backend Test Commands
---------------------

From ``src\backend``:

.. code-block:: powershell

   .\gradlew.bat clean test

Integration tests:

.. code-block:: powershell

   .\gradlew.bat integrationTest

Coverage report and gate:

.. code-block:: powershell

   .\gradlew.bat jacocoTestReport jacocoTestCoverageVerification

Frontend Tests
--------------

From ``src\frontend``:

.. code-block:: powershell

   npm test

CI Workflows in Repository
--------------------------

The repository currently defines two backend GitHub Actions workflows:

* ``.github/workflows/backend-push.yml``
* ``.github/workflows/backend-pull-req.yml``

They include:

* Java 21 + Gradle setup
* Unit tests and JaCoCo coverage verification
* Integration tests on pull requests
* Artifact upload of reports

Jenkins Requirement
-------------------

For Sprint 3 review, Jenkins should be configured to execute at least:

* backend build
* unit tests
* integration tests

and expose test/coverage reports in the Jenkins dashboard.

Testing and CI/CD
=================

Backend Testing
---------------

**Unit Tests**

Run only unit tests (excludes integration tests):

.. code-block:: powershell

   cd src\backend
   .\gradlew.bat clean test

Unit tests use mocking and are fast, without external dependencies.

**Integration Tests**

Run integration tests with Testcontainers (real PostgreSQL):

.. code-block:: powershell

   cd src\backend
   .\gradlew.bat integrationTest

Integration tests spin up temporary PostgreSQL containers and clean up automatically.

**All Tests**

Run all tests (unit + integration):

.. code-block:: powershell

   cd src\backend
   .\gradlew.bat clean test integrationTest

**Code Coverage Report**

Generate JaCoCo coverage report and verify against minimum thresholds:

.. code-block:: powershell

   cd src\backend
   .\gradlew.bat jacocoTestReport jacocoTestCoverageVerification

Coverage report location:

.. code-block:: text

   build/reports/jacoco/test/html/index.html

If coverage falls below configured thresholds, the build fails and prevents commits.

**Architecture Tests**

ArchUnit tests verify code structure compliance:

.. code-block:: powershell

   cd src\backend
   .\gradlew.bat test -Dgroups=architecture

Frontend Testing
----------------

**Playwright E2E Tests**

From ``src\frontend``, run end-to-end tests:

.. code-block:: powershell

   npm install
   npm test

Playwright tests run across three device profiles:

* Desktop
* Tablet
* Mobile

**Run Specific Device Profile**

.. code-block:: powershell

   npm run test:desktop
   npm run test:tablet
   npm run test:mobile

**View Test Report**

After running tests, view the HTML report:

.. code-block:: powershell

   npm run test:report

Opens ``playwright-report/index.html`` in your browser.

**Debug Tests Interactively**

Use Playwright Inspector for interactive debugging:

.. code-block:: powershell

   npx playwright test --debug

GitHub Actions CI Workflows
----------------------------

The repository defines automated CI workflows in ``.github/workflows/``:

**backend-push.yml** - Triggered on push to main branch
   * Runs unit tests
   * Runs JaCoCo coverage verification
   * Enforces minimum coverage thresholds
   * Uploads coverage reports

**backend-pull-req.yml** - Triggered on pull requests
   * Runs unit tests
   * Runs integration tests (full suite with Testcontainers)
   * Runs JaCoCo coverage verification
   * Uploads coverage and test reports

**sphinx-docs.yml** - Documentation workflow
   * Builds Sphinx documentation on push
   * Deploys docs to GitHub Pages (if configured)

Performance Testing
-------------------

**k6 Performance Tests**

k6 scripts are located in ``src/backend/src/test/performance/``.

Run performance tests locally:

.. code-block:: powershell

   docker-compose up performance

The k6 runner container executes ``/scripts/main.js`` and reports metrics.

Example k6 test structure:

.. code-block:: javascript

   import http from 'k6/http';
   import { check } from 'k6';

   export default function () {
     let response = http.get('http://backend:10000/api/health');
     check(response, {
       'status is 200': (r) => r.status === 200,
     });
   }

View test results in k6 console output.

Test Execution in CI
--------------------

**On Push to Main:**

.. code-block:: text

   backend-push.yml
   ├─ Checkout code
   ├─ Setup Java 21
   ├─ Run ./gradlew test
   ├─ Run ./gradlew jacocoTestReport jacocoTestCoverageVerification
   └─ Upload reports to GitHub Actions artifacts

**On Pull Request:**

.. code-block:: text

   backend-pull-req.yml
   ├─ Checkout code
   ├─ Setup Java 21
   ├─ Run ./gradlew test (unit tests)
   ├─ Run ./gradlew integrationTest (full integration)
   ├─ Run ./gradlew jacocoTestCoverageVerification
   └─ Upload reports to GitHub Actions artifacts

**Documentation Build:**

.. code-block:: text

   sphinx-docs.yml
   ├─ Checkout code
   ├─ Setup Python
   ├─ Install Sphinx dependencies
   ├─ Build HTML documentation
   └─ Deploy to GitHub Pages (optional)

Continuous Integration Practices
---------------------------------

* **Fail-Fast:** Tests run immediately on push/PR, blocking merge if they fail
* **Coverage Gating:** JaCoCo enforces minimum coverage thresholds per module
* **Integration Testing:** PRs require full integration test suite to pass
* **Artifact Preservation:** Test reports and coverage reports uploaded for analysis
* **Automated Documentation:** Sphinx builds automatically on every commit

Best Practices for Testing
---------------------------

* Write tests alongside features (TDD approach)
* Keep unit tests fast (< 1 second per test)
* Use Testcontainers for integration tests with real databases
* Mock external services (Judge0, etc.) in unit tests
* Run full test suite before pushing to avoid CI failures
* Review coverage reports to identify untested code paths
* Use Playwright for critical user workflows (auth, submission, results)

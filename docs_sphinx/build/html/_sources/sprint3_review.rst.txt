GitHub Actions Workflows
========================

Overview
--------

BSPQ26-E8 uses **GitHub Actions** for continuous integration and documentation deployment.
All workflows are defined in ``.github/workflows/``.

Backend Workflows
-----------------

**backend-push.yml** - Triggered on push to main branch
   Runs on every commit to main:

   * Checkout code
   * Setup Java 21 environment
   * Run unit tests (excluding integration tests)
   * Generate and verify JaCoCo coverage
   * Enforce minimum coverage thresholds
   * Upload test reports and coverage artifacts
   * Block merge if tests fail or coverage is below threshold

**backend-pull-req.yml** - Triggered on pull requests
   Runs on every PR to validate changes:

   * Checkout code
   * Setup Java 21 environment
   * Run complete test suite (unit + integration tests with Testcontainers)
   * Verify JaCoCo coverage gates
   * Upload detailed test reports
   * Upload coverage reports to GitHub Actions artifacts
   * Prevent PR merge if any checks fail

Documentation Workflow
----------------------

**sphinx-docs.yml** - Triggered on documentation changes
   Automatically builds Sphinx documentation:

   * Checkout code
   * Setup Python environment
   * Install Sphinx and dependencies
   * Build HTML documentation
   * Deploy to GitHub Pages (if configured)
   * Validate no build warnings/errors

Workflow Execution Flow
-----------------------

.. code-block:: text

   Developer Push to Main
   │
   ├─ backend-push.yml runs
   │  ├─ Java 21 + Gradle setup
   │  ├─ ./gradlew clean test
   │  ├─ ./gradlew jacocoTestReport
   │  ├─ Check coverage thresholds
   │  └─ Upload artifacts
   │
   └─ sphinx-docs.yml runs (if docs changed)
      ├─ Build documentation
      └─ Deploy to Pages

   Developer Opens PR
   │
   ├─ backend-pull-req.yml runs
   │  ├─ Java 21 + Gradle setup
   │  ├─ ./gradlew test (unit tests)
   │  ├─ ./gradlew integrationTest (full suite)
   │  ├─ ./gradlew jacocoTestCoverageVerification
   │  └─ Upload reports
   │
   └─ PR blocked if workflows fail

Key Features
------------

* **Fail-Fast:** All workflows must pass before code can merge
* **Coverage Gates:** JaCoCo minimum thresholds enforced in CI
* **Integration Testing:** Full Testcontainers suite on PRs
* **Artifact Preservation:** Reports available for 90 days on GitHub Actions
* **Automated Docs:** Documentation builds on every commit
* **No Manual Intervention:** All checks run automatically

Viewing Workflow Results
------------------------

1. Go to your repository on GitHub
2. Click on **Actions** tab
3. Select a workflow run to see detailed logs
4. Download artifacts (test reports, coverage reports) from the **Artifacts** section
5. Check pull request status for inline feedback

Best Practices
--------------

* Write meaningful commit messages referencing issues
* Ensure all tests pass locally before pushing (run ``./gradlew test integrationTest`` locally)
* Monitor workflow execution times and optimize if needed
* Review coverage reports in artifacts to identify gaps
* Fix CI failures immediately to avoid blocking other developers

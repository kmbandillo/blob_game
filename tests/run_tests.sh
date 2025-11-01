#!/usr/bin/env bash
# Simple test runner used by repo validation pipelines.
# Runs the Maven test suite.
set -euo pipefail

echo "Running project tests..."
if command -v mvn >/dev/null 2>&1; then
  mvn test
else
  echo "mvn not found; please install Maven or run tests from an IDE." >&2
  exit 2
fi

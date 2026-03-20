#!/bin/bash

# Script to run tests inside Docker container

echo "🧪 Running Unit Tests..."
echo ""

# Run all unit tests (excluding integration tests)
docker compose exec -T backend ./gradlew test -x integrationTest --no-daemon

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ All tests passed!"
else
    echo ""
    echo "❌ Some tests failed!"
    exit 1
fi

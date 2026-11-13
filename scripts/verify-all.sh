#!/usr/bin/env bash
# PhantomTrace — Verify Full Monorepo Build and Tests
set -e

echo "=== [1/2] Verifying Backend (Spring Boot / Maven) ==="
cd "$(dirname "$0")/../backend"
mvn clean test

echo "=== [2/2] Verifying Frontend (React / Vite) ==="
cd "../frontend"
npm run build
npm test

echo "=== All checks passed successfully! ==="

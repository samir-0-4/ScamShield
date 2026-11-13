#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

if ! grep -R "api/v1" backend/src/main/java/com/phantomtrace/controller frontend/src/services \
  >/dev/null 2>&1; then
  echo "API contract validation failed: no shared API route declarations found."
  exit 1
fi

if grep -R "axios\.\|fetch(" frontend/src/components frontend/src/pages >/dev/null 2>&1; then
  echo "API contract validation failed: UI code must use service boundaries."
  exit 1
fi

echo "API contract validation passed."

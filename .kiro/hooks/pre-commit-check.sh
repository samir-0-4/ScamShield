#!/usr/bin/env bash
# Kiro Pre-Commit Quality Hook
set -e

echo "[Kiro Hook] Checking project standards..."

# Check no committed .env files
if git status --porcelain 2>/dev/null | grep -E '^\s*[AM].*\.env$' | grep -v '.env.example'; then
  echo "ERROR: Attempted to stage a real .env file! Aborting commit."
  exit 1
fi

echo "[Kiro Hook] Pre-commit checks passed."


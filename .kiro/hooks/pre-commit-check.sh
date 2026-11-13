#!/usr/bin/env bash
# Kiro Pre-Commit Quality Hook
set -e

echo "[Kiro Hook] Checking project standards..."

# Check no staged real .env files.
if git diff --cached --name-only --diff-filter=ACM | grep -E '(^|/)\.env($|\.)' | grep -v '\.env\.example$'; then
  echo "ERROR: Attempted to stage a real .env file! Aborting commit."
  exit 1
fi

# Check staged additions for common secret assignments without flagging examples
# or documentation that describes the expected environment variables.
if git diff --cached --unified=0 -- ':!*.md' ':!*.example' \
  | grep -E '^\+[^+].*(api[_-]?key|secret|password|token)[[:space:]]*[:=][[:space:]]*[^${<[:space:]]{8,}' \
  | grep -vE '(your_|change_me|example|placeholder|test-secret|localhost)'; then
  echo "ERROR: Possible hardcoded secret detected in staged changes."
  exit 1
fi

echo "[Kiro Hook] Pre-commit checks passed."

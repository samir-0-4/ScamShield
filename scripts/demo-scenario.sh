#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${PHANTOMTRACE_API_BASE:-http://localhost:8080/api/v1}"
EMAIL="${DEMO_EMAIL:-analyst-$(date +%s)@phantomtrace.local}"
PASSWORD="${DEMO_PASSWORD:-DemoPassword123!}"

post_json() {
  curl -fsS -X POST "$1" -H "Authorization: Bearer $TOKEN" \
    -H 'Content-Type: application/json' -d "$2"
}

curl -fsS -X POST "$BASE_URL/auth/register" -H 'Content-Type: application/json' \
  -d "{\"email\":\"$EMAIL\",\"fullName\":\"PhantomTrace Demo Analyst\",\"password\":\"$PASSWORD\"}" >/dev/null

LOGIN=$(curl -fsS -X POST "$BASE_URL/auth/login" -H 'Content-Type: application/json' \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")
TOKEN=$(printf '%s' "$LOGIN" | python3 -c 'import json,sys; print(json.load(sys.stdin)["data"]["token"])')

INVESTIGATION=$(post_json "$BASE_URL/investigations" \
  '{"title":"Remote Job Offer Investigation","description":"Synthetic hackathon demonstration case","category":"JOB_SCAM"}')
INVESTIGATION_ID=$(printf '%s' "$INVESTIGATION" | python3 -c 'import json,sys; print(json.load(sys.stdin)["data"]["id"])')

MESSAGE=$(post_json "$BASE_URL/investigations/$INVESTIGATION_ID/evidence" \
  '{"evidenceType":"TEXT_MESSAGE","textContent":"Congratulations! You have been shortlisted for a remote job opportunity. To complete your employee verification, pay a registration fee of ₹499 today. Your offer will expire in two hours. Complete the process using the registration link.","description":"Synthetic job offer message"}')
MESSAGE_ID=$(printf '%s' "$MESSAGE" | python3 -c 'import json,sys; print(json.load(sys.stdin)["data"]["id"])')

URL=$(post_json "$BASE_URL/investigations/$INVESTIGATION_ID/evidence" \
  '{"evidenceType":"URL","url":"https://demo.invalid/employee-verification","description":"Reserved synthetic test domain; do not visit"}')
URL_ID=$(printf '%s' "$URL" | python3 -c 'import json,sys; print(json.load(sys.stdin)["data"]["id"])')

curl -fsS -X POST "$BASE_URL/evidence/$MESSAGE_ID/analysis" -H "Authorization: Bearer $TOKEN" >/dev/null || true
curl -fsS -X POST "$BASE_URL/evidence/$URL_ID/threat-lookups" -H "Authorization: Bearer $TOKEN" >/dev/null || true
curl -fsS -X POST "$BASE_URL/investigations/$INVESTIGATION_ID/risk-assessment" -H "Authorization: Bearer $TOKEN" >/dev/null
curl -fsS -X POST "$BASE_URL/investigations/$INVESTIGATION_ID/graph/generate" -H "Authorization: Bearer $TOKEN" >/dev/null
GRAPH=$(curl -fsS "$BASE_URL/investigations/$INVESTIGATION_ID/graph" -H "Authorization: Bearer $TOKEN")
REPORT=$(curl -fsS -X POST "$BASE_URL/investigations/$INVESTIGATION_ID/reports" -H "Authorization: Bearer $TOKEN")

python3 - "$INVESTIGATION_ID" "$GRAPH" "$REPORT" <<'PY'
import json, sys
graph = json.loads(sys.argv[2])["data"]
report = json.loads(sys.argv[3])["data"]
print(f"Investigation: {sys.argv[1]}")
print(f"Graph: {len(graph['nodes'])} nodes, {len(graph['edges'])} edges")
print(f"Report: {report['id']}")
print("External and AI calls may be unavailable; the UI labels those outcomes explicitly.")
PY

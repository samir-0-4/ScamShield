# Hackathon Demo Scenario

## Synthetic case

- **Title:** Remote Job Offer Investigation
- **Category:** `JOB_SCAM`
- **User:** `analyst@phantomtrace.local` when the optional dev seed is enabled, or a fresh synthetic account created by `scripts/demo-scenario.sh`
- **Password:** Set `PHANTOMTRACE_DEMO_SEED_PASSWORD`; never use a real personal password.

The demo message is:

> Congratulations! You have been shortlisted for a remote job opportunity. To complete your employee verification, pay a registration fee of ₹499 today. Your offer will expire in two hours. Complete the process using the registration link.

The URL is `https://demo.invalid/employee-verification`. `.invalid` is reserved for examples and must not be visited or presented as a real malicious site.

QR/image processing is not part of the current implemented path, so the demo does not claim a QR result. Image/PDF storage and validation are available.

## Reproducible setup

```bash
docker compose up -d postgres
cd backend
cp .env.example .env
# Set DATABASE_URL to the host port shown by `docker compose ps`.
mvn spring-boot:run
```

In another terminal:

```bash
./scripts/demo-scenario.sh
```

The script registers a synthetic account, creates the investigation, submits two
evidence items, attempts analysis and threat lookup, generates deterministic risk
and correlation data, and persists a report. Failed external calls are allowed so
the UI can demonstrate explicit unavailable states; this is not fallback data.

## Backup/demo reliability

The presentation-only [fallback fixture](demo-fallback.json) is not wired into
the application and is explicitly marked `live: false`. It is only a verbal
backup for a service outage. The preferred fallback remains demonstrating the
real unavailable/error states and explaining that Groq and threat providers are
optional integrations.

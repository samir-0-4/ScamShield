# PhantomTrace — Local Setup Guide

## System Prerequisites
- **Node.js**: v20.x or v24.x (`node -v`)
- **npm**: v10.x or v11.x (`npm -v`)
- **Java JDK**: 21+ (`java -version`)
- **Apache Maven**: 3.9+ (`mvn -v`)
- **Docker & Docker Compose**: For containerized PostgreSQL database

---

## Step 1: Environment Configuration

Copy the root `.env.example` into `.env` (or configure separate frontend/backend `.env` files):

```bash
# In the root repository directory
cp .env.example .env
```

Generate and set a development JWT secret before starting the backend:

```bash
openssl rand -base64 32
```

Set the generated value as `JWT_SECRET` in `backend/.env` (or in the shell
environment). The application rejects blank, weak, known placeholder, and
legacy default values. Do not commit the generated secret.

---

## Step 2: Start PostgreSQL Database

Launch the local database container:

```bash
docker compose up -d postgres
```

Verify that the database is healthy:

```bash
docker compose ps
```

The published host port is controlled by `POSTGRES_PORT`. If it is overridden
in your local `.env`, use the same port in `DATABASE_URL`.

---

## Step 3: Run the Backend

```bash
cd backend
cp .env.example .env

# Verify tests and build
mvn clean test

# Launch the development server
mvn spring-boot:run
```

Check health:
```bash
curl http://localhost:8080/api/v1/health
```

---

## Step 4: Run the Frontend

```bash
cd frontend
cp .env.example .env

# Install dependencies
npm install

# Start Vite dev server
npm run dev
```

The frontend will be running at `http://localhost:5173`.

## File analysis dependencies

The backend bundles PDFBox and ZXing. Image OCR uses Tess4J and requires a
local Tesseract installation with language data; without it, OCR is reported as
unavailable while QR decoding and ordinary image validation continue to work.
Decoded QR URLs are analyzed as untrusted evidence and are never opened by the
application.

## Production configuration

Set `SPRING_PROFILES_ACTIVE=prod` and provide `JWT_SECRET` through the
deployment secret manager. Production startup fails when the secret is missing
or insecure. Do not use values from `.env.example`, and do not generate a new
secret on every restart because existing JWT sessions would become invalid.

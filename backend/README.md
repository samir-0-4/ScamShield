# PhantomTrace — Backend API Service

Enterprise Spring Boot 3 service running on Java 21, coordinating evidence parsing, database persistence, external threat intelligence lookups, and multi-agent AI orchestration.

---

## 🛠️ Tech Stack
- **Framework**: Spring Boot 3.4.2
- **Java**: Version 21
- **Security**: Spring Security 6 (Stateless JWT architecture)
- **Persistence**: Spring Data JPA + Hibernate
- **Database**: PostgreSQL 16 + Flyway migrations
- **AI Engine**: Groq API (OpenAI-compatible client abstraction)
- **Threat Intel**: VirusTotal & URLhaus API clients

---

## 🚀 Running Locally

```bash
# 1. Start PostgreSQL (from project root)
docker compose up -d postgres

# 2. Configure environment
cp .env.example .env

# 3. Compile and run unit tests
mvn clean test

# 4. Run application
mvn spring-boot:run
```

## 🩺 Health Check Endpoint
```bash
curl http://localhost:8080/api/v1/health
# Response:
# {"status":"UP","application":"PhantomTrace"}
```


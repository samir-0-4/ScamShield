# Design Document: Phase 1 — Project Verification

> **Spec ID:** `project-verification`  
> **Phase:** 1 (Verification & Baseline)  

## 1. Technical Strategy

### 1.1 Maven Surefire Configuration
To ensure deterministic execution across Java 21 and Java 23 runtimes without developer environment friction, add the `maven-surefire-plugin` configuration block to `backend/pom.xml`:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.5.2</version>
    <configuration>
        <argLine>-XX:+EnableDynamicAgentLoading -Dnet.bytebuddy.experimental=true</argLine>
    </configuration>
</plugin>
```

### 1.2 Environment Isolation
Ensure the port mapping strategy across the repository is unified:
- `docker-compose.yml`: Uses `${POSTGRES_PORT:-5434}:5432` to guard against native host PostgreSQL collision on `5432`.
- `.env.example` & `backend/.env.example`: Documents `POSTGRES_PORT=5434` and `DATABASE_URL=jdbc:postgresql://localhost:5434/phantomtrace`.
- Frontend `Vite` dev server uses port `5173` with proxy configuration to `http://localhost:8080`.
- Spring Boot server uses port `8080` with CORS configuration authorizing `http://localhost:5173`.

### 1.3 Health Check Verification
`HealthController` in `com.phantomtrace.controller`:
- Path: `GET /api/v1/health`
- Envelope: `HealthResponse(String status, String application)`
- Security: Explicitly permitted in `SecurityConfig.securityFilterChain` without JWT requirement.


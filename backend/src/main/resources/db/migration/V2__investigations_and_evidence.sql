-- ==============================================================================
-- PhantomTrace Flyway Migration V2 — Investigations and Evidence Schema
-- Establishes core case management and evidence storage tables.
-- ==============================================================================

-- Investigations dossier table
CREATE TABLE investigations (
    id                 UUID             PRIMARY KEY NOT NULL,
    user_id            UUID             NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title              VARCHAR(255)     NOT NULL,
    description        TEXT,
    category           VARCHAR(50)      NOT NULL,
    status             VARCHAR(50)      NOT NULL DEFAULT 'DRAFT',
    overall_risk_level VARCHAR(50)      DEFAULT 'UNKNOWN',
    risk_score         DOUBLE PRECISION DEFAULT 0.0,
    created_at         TIMESTAMPTZ      NOT NULL,
    updated_at         TIMESTAMPTZ      NOT NULL,
    completed_at       TIMESTAMPTZ
);

-- Performance and lookup indexes for investigations
CREATE INDEX idx_investigations_user_id ON investigations (user_id);
CREATE INDEX idx_investigations_status ON investigations (status);
CREATE INDEX idx_investigations_user_created ON investigations (user_id, created_at DESC);

-- Evidence items attached to investigations
CREATE TABLE evidence (
    id                 UUID         PRIMARY KEY NOT NULL,
    investigation_id   UUID         NOT NULL REFERENCES investigations(id) ON DELETE CASCADE,
    evidence_type      VARCHAR(50)  NOT NULL,
    original_filename  VARCHAR(255),
    storage_key        VARCHAR(512),
    text_content       TEXT,
    normalized_content TEXT,
    description        TEXT,
    mime_type          VARCHAR(100),
    file_size          BIGINT,
    status             VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    created_at         TIMESTAMPTZ  NOT NULL
);

-- Performance and lookup indexes for evidence
CREATE INDEX idx_evidence_investigation_id ON evidence (investigation_id);
CREATE INDEX idx_evidence_status ON evidence (status);


CREATE TABLE risk_assessments (
    id                   UUID PRIMARY KEY NOT NULL,
    investigation_id     UUID NOT NULL REFERENCES investigations(id) ON DELETE CASCADE,
    score                INTEGER NOT NULL,
    category             VARCHAR(20) NOT NULL,
    confidence           DOUBLE PRECISION NOT NULL,
    missing_information  TEXT,
    explanation          TEXT NOT NULL,
    assessed_at          TIMESTAMPTZ NOT NULL
);

CREATE TABLE risk_contributions (
    id                    UUID PRIMARY KEY NOT NULL,
    assessment_id         UUID NOT NULL REFERENCES risk_assessments(id) ON DELETE CASCADE,
    signal_key             VARCHAR(80) NOT NULL,
    signal                 VARCHAR(120) NOT NULL,
    weight                 INTEGER NOT NULL,
    evidence_references    TEXT NOT NULL,
    explanation            TEXT NOT NULL
);

CREATE INDEX idx_risk_assessments_investigation ON risk_assessments (investigation_id, assessed_at DESC);
CREATE INDEX idx_risk_contributions_assessment ON risk_contributions (assessment_id);

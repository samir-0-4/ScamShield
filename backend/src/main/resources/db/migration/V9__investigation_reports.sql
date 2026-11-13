CREATE TABLE investigation_reports (
    id              UUID PRIMARY KEY NOT NULL,
    investigation_id UUID NOT NULL REFERENCES investigations(id) ON DELETE CASCADE,
    content_json    TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_investigation_reports_investigation
    ON investigation_reports (investigation_id, created_at DESC);

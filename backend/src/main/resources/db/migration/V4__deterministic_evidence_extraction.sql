CREATE TABLE extracted_indicators (
    id                 UUID         PRIMARY KEY NOT NULL,
    evidence_id        UUID         NOT NULL REFERENCES evidence(id) ON DELETE CASCADE,
    indicator_type     VARCHAR(50)  NOT NULL,
    original_value     TEXT         NOT NULL,
    normalized_value   TEXT,
    extraction_method  VARCHAR(100) NOT NULL,
    extracted_at       TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_extracted_indicators_evidence_id
    ON extracted_indicators (evidence_id);

CREATE INDEX idx_extracted_indicators_type
    ON extracted_indicators (indicator_type);

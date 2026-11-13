CREATE TABLE threat_indicators (
    id                          UUID PRIMARY KEY NOT NULL,
    investigation_id            UUID NOT NULL REFERENCES investigations(id) ON DELETE CASCADE,
    indicator_type              VARCHAR(50) NOT NULL,
    indicator_value             TEXT NOT NULL,
    normalized_value            TEXT NOT NULL,
    source_evidence_id          UUID NOT NULL REFERENCES evidence(id) ON DELETE CASCADE,
    external_verification_status VARCHAR(40) NOT NULL,
    created_at                  TIMESTAMPTZ NOT NULL
);

CREATE TABLE evidence_relationships (
    id                    UUID PRIMARY KEY NOT NULL,
    investigation_id      UUID NOT NULL REFERENCES investigations(id) ON DELETE CASCADE,
    source_indicator_id   UUID REFERENCES threat_indicators(id) ON DELETE CASCADE,
    target_indicator_id   UUID REFERENCES threat_indicators(id) ON DELETE CASCADE,
    source_evidence_id    UUID NOT NULL REFERENCES evidence(id) ON DELETE CASCADE,
    target_evidence_id    UUID NOT NULL REFERENCES evidence(id) ON DELETE CASCADE,
    relationship_type     VARCHAR(50) NOT NULL,
    verification_status   VARCHAR(40) NOT NULL,
    confidence            DOUBLE PRECISION NOT NULL,
    explanation           TEXT NOT NULL,
    source_reference      VARCHAR(255),
    created_at            TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_threat_indicators_investigation ON threat_indicators (investigation_id);
CREATE INDEX idx_threat_indicators_normalized ON threat_indicators (investigation_id, indicator_type, normalized_value);
CREATE INDEX idx_relationships_investigation ON evidence_relationships (investigation_id);
CREATE INDEX idx_relationships_source_evidence ON evidence_relationships (source_evidence_id);
CREATE INDEX idx_relationships_target_evidence ON evidence_relationships (target_evidence_id);

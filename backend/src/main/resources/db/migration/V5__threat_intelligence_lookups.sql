CREATE TABLE threat_lookups (
    id                 UUID PRIMARY KEY NOT NULL,
    indicator_id       UUID NOT NULL REFERENCES extracted_indicators(id) ON DELETE CASCADE,
    provider           VARCHAR(40) NOT NULL,
    indicator_value    TEXT NOT NULL,
    lookup_status      VARCHAR(40) NOT NULL,
    threat_status      VARCHAR(40) NOT NULL,
    result_details     TEXT,
    provider_reference VARCHAR(255),
    error_message      TEXT,
    lookup_timestamp   TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_threat_lookups_indicator_id ON threat_lookups (indicator_id);
CREATE INDEX idx_threat_lookups_timestamp ON threat_lookups (lookup_timestamp);

CREATE TABLE agent_executions (
    id                    UUID PRIMARY KEY NOT NULL,
    evidence_id           UUID NOT NULL REFERENCES evidence(id) ON DELETE CASCADE,
    agent_name            VARCHAR(80) NOT NULL,
    model                 VARCHAR(120) NOT NULL,
    execution_status      VARCHAR(40) NOT NULL,
    duration_milliseconds BIGINT NOT NULL,
    output_json           TEXT,
    error_message         TEXT,
    completed_at          TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_agent_executions_evidence_id ON agent_executions (evidence_id);

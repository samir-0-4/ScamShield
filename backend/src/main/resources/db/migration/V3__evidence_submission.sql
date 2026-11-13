ALTER TABLE evidence ADD COLUMN url VARCHAR(2048);
ALTER TABLE evidence ADD COLUMN sender_address VARCHAR(320);
ALTER TABLE evidence ADD COLUMN subject VARCHAR(998);
ALTER TABLE evidence ADD COLUMN email_body TEXT;
ALTER TABLE evidence ADD COLUMN email_headers TEXT;
ALTER TABLE evidence ADD COLUMN content_hash VARCHAR(64);

CREATE INDEX idx_evidence_investigation_hash ON evidence (investigation_id, content_hash);

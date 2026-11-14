-- ==============================================================================
-- PhantomTrace Flyway Migration V10 — Persistent Synthetic Demo Marker
-- Establishes dedicated provenance column to distinguish synthetic demonstration
-- investigations from genuine security dossiers.
-- ==============================================================================

ALTER TABLE investigations ADD COLUMN synthetic_demo BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_investigations_user_synthetic_demo ON investigations (user_id, synthetic_demo);


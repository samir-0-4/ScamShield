# Design — Evidence Correlation

`IndicatorNormalizationService` canonicalizes values by indicator type. `IndicatorMatchingService`
groups extracted indicators by type and normalized value. `EvidenceRelationshipService` creates
deterministic relationships and threat matches with explicit provenance. `CorrelationOrchestrator`
owns investigation authorization, idempotent rebuilding, persistence, and graph projection.

Threat indicators are derived from extracted indicators and current provider lookups. External
verification status is copied as metadata; unavailable provider data remains unavailable and is
never treated as clean. AI outputs are not used to create confirmed graph edges.

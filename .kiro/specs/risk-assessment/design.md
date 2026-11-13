# Design — Risk Assessment

`RiskAssessmentService` reads extracted indicators, threat lookups, relationships,
and evidence content. `RiskAssessmentProperties` supplies configurable initial
weights. A normalized assessment row is accompanied by contribution rows so every
point is explainable and traceable to evidence.

Credential/OTP, payment, impersonation, urgency/pressure, and unusual URL signals
are deduplicated by normalized value and rule key. Verified malicious indicators
are deduplicated by normalized indicator. Missing provider data lowers support
confidence but never creates a clean result.

AI findings are not used as authoritative scoring inputs in this phase.

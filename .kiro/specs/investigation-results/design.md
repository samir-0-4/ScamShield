# Design — Investigation Results

`AnalysisResultsPage` loads investigation metadata, evidence, latest risk assessment,
analysis executions, indicators, threat lookups, and graph data through centralized
services. Structured agent output is parsed from the backend execution response and
flattened into reusable finding cards.

The page uses the existing ScamShield cards, badges, spacing, and responsive grid.
Provider errors and unavailable results are displayed as unknown/unavailable rather
than clean. Recommendations are derived only from returned findings and deterministic
indicators.

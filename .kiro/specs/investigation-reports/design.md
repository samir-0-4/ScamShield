# Design

Reports are immutable JSON snapshots associated with an investigation. Creation
verifies ownership, reads persisted analysis executions and current risk/graph
data, and stores the normalized report payload. The frontend renders the latest
snapshot and uses browser print CSS for PDF export.

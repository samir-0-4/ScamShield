# Design — Evidence Relationship Graph

`EvidenceGraphPage` retrieves the investigation and graph through centralized
services. Backend `GraphNode` records are mapped directly to React Flow nodes;
backend `GraphEdge` records are mapped directly to labeled React Flow edges.
Positions are presentation-only and are calculated from the returned node list.

Selection state is local to the page. A selected node shows its backend type,
value, source evidence ID, verification status, and related structured findings
when available. A selected edge shows its relationship explanation, evidence
references, confidence, and verification state.

React Flow controls remain usable on small screens; the detail panel moves below
the graph on narrow viewports.

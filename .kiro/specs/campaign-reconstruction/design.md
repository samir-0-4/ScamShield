# Design — Multi-Case Campaign Reconstruction & Attribution Engine

## Architecture

```
User Investigation (Case A)
   └── Indicators (domain, IP, URL, email)
              ▲
              │ Normalized Indicator Matching Engine
              ▼
Tenant Investigation Index (Cases B, C...)
   └── Indicators (shared domain, shared IP)
              │
              ▼
CampaignReconstructionService
   ├── Cross-Investigation Correlation
   ├── Provider Threat Verification (VT/URLhaus)
   ├── Classification Engine (ISOLATED | POSSIBLE | PROBABLE | CONFIRMED)
   └── Forensic Narrative Synthesizer
              │
              ▼
CampaignReconstructionResponse & Campaign Graph View
```

## Data Transfer Objects

1. `CampaignReconstructionResponse`:
   - `sharedIndicators`: List<SharedIndicatorDto> (with crossInvestigation flags and verification status)
   - `campaignSignals`: List<CampaignSignalDto> (with supporting investigations and explanations)
   - `connectedInvestigations`: List<ConnectedInvestigationDto>
   - `campaignClassification`: String
   - `attributionSummary`: String
   - `totalConnectedInvestigations`: int
   - `totalLinkedEvidenceItems`: int

2. `ConnectedInvestigationDto`:
   - `id`, `title`, `category`, `status`, `sharedIndicatorCount`, `sharedIndicatorValues`, `relationshipExplanation`, `confidence`

## Security Controls

- Investigation ownership verified via `SecurityUtils.getCurrentUserId()`.
- Cross-case correlation strictly bounded by `evidence.investigation.user.id == currentUserId`.
- Generic email providers (`gmail.com`, `yahoo.com`, etc.) excluded from bare domain correlation.

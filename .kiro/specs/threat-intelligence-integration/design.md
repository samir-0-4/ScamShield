# Design — Threat Intelligence Integration

`VirusTotalClient` and `UrlhausClient` expose typed lookup contracts. Their
implementations own HTTP paths, headers, request encoding, and provider JSON parsing.
`ThreatIntelligenceService` coordinates URL indicators, caches normalized results,
persists them, and enforces evidence ownership through the existing service.

Provider calls use Spring `RestClient` with fixed provider base URLs, explicit
timeouts, no redirect-following behavior, and no arbitrary user-supplied target.
VirusTotal URL identifiers use URL-safe base64 without padding. URLhaus receives the
validated original URL as form data.

The persistence model is intentionally separate from future analysis results and
threat indicator tables. Lookup records are linked to an extracted indicator and
deleted with its evidence.

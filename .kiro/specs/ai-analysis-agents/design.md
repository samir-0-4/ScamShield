# Design — AI Analysis Agents

`AiProvider` is the only model boundary. `GroqAiProvider` uses a single
`GroqProperties`/`AIModelConfig` configuration and returns typed completion results.
`StructuredAiResponseParser` validates a strict JSON envelope containing findings and
indicators.

`TextDetectiveAgent`, `UrlDetectiveAgent`, `EmailDetectiveAgent`,
`IntelligenceCorrelationAgent`, and `InvestigationReportAgent` each build a bounded
task prompt and consume the same provider. Agents never execute tools.

`AiAgentOrchestrator` validates evidence ownership, uses existing deterministic
indicators, selects applicable agents, runs them sequentially, validates outputs,
stores an `AgentExecution`, and returns normalized results. A failed agent does not
erase deterministic evidence or produce a clean verdict.

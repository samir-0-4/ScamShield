package com.phantomtrace.ai.orchestrator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phantomtrace.ai.agent.AiAgent;
import com.phantomtrace.ai.config.AIProviderConfig;
import com.phantomtrace.ai.dto.AgentAnalysisResponse;
import com.phantomtrace.ai.dto.AgentExecutionResponse;
import com.phantomtrace.ai.dto.StructuredAiResponse;
import com.phantomtrace.ai.provider.AiProviderResult;
import com.phantomtrace.entity.AgentExecution;
import com.phantomtrace.entity.Evidence;
import com.phantomtrace.entity.ExtractedIndicator;
import com.phantomtrace.enums.EvidenceType;
import com.phantomtrace.enums.IndicatorType;
import com.phantomtrace.repository.AgentExecutionRepository;
import com.phantomtrace.repository.EvidenceRepository;
import com.phantomtrace.repository.ExtractedIndicatorRepository;
import com.phantomtrace.service.EvidenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AiAgentOrchestrator {

    private final Map<String, AiAgent> agents;
    private final EvidenceService evidenceService;
    private final EvidenceRepository evidenceRepository;
    private final ExtractedIndicatorRepository indicatorRepository;
    private final AgentExecutionRepository executionRepository;
    private final ObjectMapper objectMapper;
    private final AIProviderConfig providerConfig;

    public AiAgentOrchestrator(List<AiAgent> agentList, EvidenceService evidenceService,
            EvidenceRepository evidenceRepository, ExtractedIndicatorRepository indicatorRepository,
            AgentExecutionRepository executionRepository, ObjectMapper objectMapper,
            AIProviderConfig providerConfig) {
        this.agents = agentList.stream().collect(Collectors.toMap(AiAgent::getAgentName, agent -> agent));
        this.evidenceService = evidenceService;
        this.evidenceRepository = evidenceRepository;
        this.indicatorRepository = indicatorRepository;
        this.executionRepository = executionRepository;
        this.objectMapper = objectMapper;
        this.providerConfig = providerConfig;
    }

    public AiAgent getAgent(String agentName) {
        return agents.get(agentName);
    }

    public List<String> getAvailableAgents() {
        return List.copyOf(agents.keySet());
    }

    public List<AgentAnalysisResponse> analyze(UUID evidenceId) {
        evidenceService.get(evidenceId);
        Evidence evidence = evidenceRepository.findById(evidenceId).orElseThrow();
        List<ExtractedIndicator> indicators = indicatorRepository.findByEvidenceIdOrderByExtractedAtAsc(evidenceId);
        List<String> selected = new ArrayList<>();
        if (evidence.getEvidenceType() == EvidenceType.TEXT_MESSAGE) selected.add("TEXT_DETECTIVE");
        if (evidence.getEvidenceType() == EvidenceType.URL
                || indicators.stream().anyMatch(value -> value.getIndicatorType() == IndicatorType.URL)) {
            selected.add("URL_DETECTIVE");
        }
        if (evidence.getEvidenceType() == EvidenceType.EMAIL) selected.add("EMAIL_DETECTIVE");
        if (evidence.getEvidenceType() == EvidenceType.IMAGE
                || evidence.getEvidenceType() == EvidenceType.PDF) selected.add("FILE_ANALYST");
        return selected.stream().map(name -> execute(name, evidence, indicators)).toList();
    }

    @Transactional(readOnly = true)
    public List<AgentExecutionResponse> listExecutions(UUID evidenceId) {
        evidenceService.get(evidenceId);
        return executionRepository.findByEvidenceIdOrderByCompletedAtDesc(evidenceId).stream()
                .map(AgentExecutionResponse::fromEntity).toList();
    }

    private AgentAnalysisResponse execute(String name, Evidence evidence, List<ExtractedIndicator> indicators) {
        AiAgent agent = agents.get(name);
        long started = System.nanoTime();
        try {
            StructuredAiResponse result = agent.analyze(evidence, indicators);
            long duration = elapsed(started);
            String json = objectMapper.writeValueAsString(result);
            AgentExecution saved = executionRepository.save(new AgentExecution(evidence, name,
                    providerConfig.getModel(), "COMPLETED", duration, json, null, Instant.now()));
            return new AgentAnalysisResponse(saved.getId(), evidence.getId(), name, providerConfig.getModel(),
                    AiProviderResult.Status.COMPLETED, duration, result, null, saved.getCompletedAt());
        } catch (Exception ex) {
            long duration = elapsed(started);
            String error = safeError(ex);
            AgentExecution saved = executionRepository.save(new AgentExecution(evidence, name,
                    providerConfig.getModel(), "FAILED", duration, null, error, Instant.now()));
            return AgentAnalysisResponse.failure(evidence.getId(), name, providerConfig.getModel(),
                    AiProviderResult.Status.ERROR, duration, error);
        }
    }

    private long elapsed(long started) {
        return (System.nanoTime() - started) / 1_000_000;
    }

    private String safeError(Exception ex) {
        return ex.getMessage() == null || ex.getMessage().isBlank() ? "Agent execution failed" : ex.getMessage();
    }
}

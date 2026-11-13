package com.phantomtrace.service;

import com.phantomtrace.entity.ExtractedIndicator;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IndicatorMatchingService {

    private final IndicatorNormalizationService normalizationService;

    public IndicatorMatchingService(IndicatorNormalizationService normalizationService) {
        this.normalizationService = normalizationService;
    }

    public Map<String, List<ExtractedIndicator>> group(List<ExtractedIndicator> indicators) {
        return indicators.stream().collect(Collectors.groupingBy(
                value -> value.getIndicatorType().name() + "\u0000"
                        + normalizationService.normalize(value.getIndicatorType(), value.getNormalizedValue()),
                LinkedHashMap::new, Collectors.toList()));
    }

    public boolean same(ExtractedIndicator left, ExtractedIndicator right) {
        return left.getIndicatorType() == right.getIndicatorType()
                && normalizationService.normalize(left.getIndicatorType(), left.getNormalizedValue())
                .equals(normalizationService.normalize(right.getIndicatorType(), right.getNormalizedValue()));
    }
}

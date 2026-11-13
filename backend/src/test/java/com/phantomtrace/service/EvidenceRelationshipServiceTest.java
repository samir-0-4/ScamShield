package com.phantomtrace.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EvidenceRelationshipServiceTest {
    @Test
    void rejectsInvalidRelationshipData() {
        assertThatThrownBy(() -> new EvidenceRelationshipService().create(
                null, null, null, null, null, null, null, 2.0, "", null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

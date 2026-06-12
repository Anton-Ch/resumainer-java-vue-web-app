package com.resumainer.dto.generate;

import java.util.Map;
import java.util.UUID;

/**
 * Updated review values from the frontend.
 * Maps field paths to new values: { "sectionKey.recordId.fieldName": "new value" }
 */
public class GenerationReviewUpdateDto {

    private UUID requestId;
    private Map<String, String> fieldUpdates;

    public GenerationReviewUpdateDto() {}

    public UUID getRequestId() { return requestId; }
    public void setRequestId(UUID requestId) { this.requestId = requestId; }

    public Map<String, String> getFieldUpdates() { return fieldUpdates; }
    public void setFieldUpdates(Map<String, String> fieldUpdates) { this.fieldUpdates = fieldUpdates; }
}

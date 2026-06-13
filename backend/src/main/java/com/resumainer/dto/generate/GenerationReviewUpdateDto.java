package com.resumainer.dto.generate;

import java.util.Map;
import java.util.UUID;

/**
 * Updated review values from the frontend.
 * fieldUpdates maps backend-generated updateKey (returned by GenerationReviewDto.AdaptationVariant)
 * to the new user-reviewed value. Frontend must not construct update keys manually;
 * it must reuse updateKey values returned by the review endpoint.
 *
 * Format: "sectionKey:recordId:fieldName:adaptationCode"
 *   sectionKey: professional_positioning, work_experience, courses, projects, skills, personal_information
 *   recordId:   UUID of the response or child record
 *   fieldName:  frontend-friendly field name (mapped to DB column by backend allowlist)
 *   adaptationCode: MINIMAL, BALANCED, MAXIMUM
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

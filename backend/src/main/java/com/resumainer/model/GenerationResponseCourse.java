package com.resumainer.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Generated course/certificate entry per response.
 * Maps to 'generation_response_course' table (UUID PK).
 */
public class GenerationResponseCourse {

    private UUID id;
    private UUID responseId;
    private String sourceId;

    private String name;
    private String provider;
    private boolean isFirstPage;
    private String courseFocus;
    private int orderInResume;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public GenerationResponseCourse() {
    }

    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getResponseId() { return responseId; }
    public void setResponseId(UUID responseId) { this.responseId = responseId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public boolean isFirstPage() { return isFirstPage; }
    public void setFirstPage(boolean firstPage) { isFirstPage = firstPage; }

    public String getCourseFocus() { return courseFocus; }
    public void setCourseFocus(String courseFocus) { this.courseFocus = courseFocus; }

    public int getOrderInResume() { return orderInResume; }
    public void setOrderInResume(int orderInResume) { this.orderInResume = orderInResume; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

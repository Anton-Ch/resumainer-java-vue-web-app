package com.resumainer.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Generated work experience entry per response.
 * Maps to 'generation_response_experience' table (UUID PK).
 */
public class GenerationResponseExperience {

    private UUID id;
    private UUID responseId;
    private String sourceId;

    private String jobTitle;
    private String companyName;
    private String description;
    private String location;
    private boolean isFirstPage;
    private LocalDate startDate;
    private LocalDate endDate;
    private int orderInResume;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<GenerationResponseExperienceBullet> bullets = new ArrayList<>();

    public GenerationResponseExperience() {
    }

    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getResponseId() { return responseId; }
    public void setResponseId(UUID responseId) { this.responseId = responseId; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public boolean isFirstPage() { return isFirstPage; }
    public void setFirstPage(boolean firstPage) { isFirstPage = firstPage; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public int getOrderInResume() { return orderInResume; }
    public void setOrderInResume(int orderInResume) { this.orderInResume = orderInResume; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<GenerationResponseExperienceBullet> getBullets() { return bullets; }
    public void setBullets(List<GenerationResponseExperienceBullet> bullets) { this.bullets = bullets; }
}

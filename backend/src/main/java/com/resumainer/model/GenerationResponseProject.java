package com.resumainer.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GenerationResponseProject {
    private UUID id; private UUID responseId;
    private String sourceId;
    private String projectName; private String role; private String description;
    private String location; private LocalDate startDate; private LocalDate endDate;
    private int orderInResume;
    private LocalDateTime createdAt; private LocalDateTime updatedAt;

    private List<GenerationResponseProjectBullet> bullets = new ArrayList<>();

    public GenerationResponseProject() {}

    public UUID getId() { return id; } public void setId(UUID id) { this.id = id; }
    public UUID getResponseId() { return responseId; } public void setResponseId(UUID responseId) { this.responseId = responseId; }
    public String getSourceId() { return sourceId; } public void setSourceId(String sourceId) { this.sourceId = sourceId; }
    public String getProjectName() { return projectName; } public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getRole() { return role; } public void setRole(String role) { this.role = role; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; } public void setLocation(String location) { this.location = location; }
    public LocalDate getStartDate() { return startDate; } public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; } public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public int getOrderInResume() { return orderInResume; } public void setOrderInResume(int orderInResume) { this.orderInResume = orderInResume; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<GenerationResponseProjectBullet> getBullets() { return bullets; } public void setBullets(List<GenerationResponseProjectBullet> bullets) { this.bullets = bullets; }
}

package com.resumainer.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class GenerationResponseSkill {
    private UUID id; private UUID responseId;
    private String skillGroup; private String skillName;
    private int orderInResume;
    private LocalDateTime createdAt; private LocalDateTime updatedAt;

    public GenerationResponseSkill() {}

    public UUID getId() { return id; } public void setId(UUID id) { this.id = id; }
    public UUID getResponseId() { return responseId; } public void setResponseId(UUID responseId) { this.responseId = responseId; }
    public String getSkillGroup() { return skillGroup; } public void setSkillGroup(String skillGroup) { this.skillGroup = skillGroup; }
    public String getSkillName() { return skillName; } public void setSkillName(String skillName) { this.skillName = skillName; }
    public int getOrderInResume() { return orderInResume; } public void setOrderInResume(int orderInResume) { this.orderInResume = orderInResume; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

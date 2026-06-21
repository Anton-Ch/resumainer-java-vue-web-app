package com.resumainer.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Ordered bullet point for a generated work experience entry.
 * Maps to 'generation_response_experience_bullet' table (BIGSERIAL PK).
 */
public class GenerationResponseExperienceBullet {

    private long id;
    private UUID experienceId;
    private int bulletOrder;
    private String bulletText;
    private boolean isEdited;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public GenerationResponseExperienceBullet() {
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public UUID getExperienceId() { return experienceId; }
    public void setExperienceId(UUID experienceId) { this.experienceId = experienceId; }

    public int getBulletOrder() { return bulletOrder; }
    public void setBulletOrder(int bulletOrder) { this.bulletOrder = bulletOrder; }

    public String getBulletText() { return bulletText; }
    public void setBulletText(String bulletText) { this.bulletText = bulletText; }

    public boolean isEdited() { return isEdited; }
    public void setEdited(boolean edited) { isEdited = edited; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

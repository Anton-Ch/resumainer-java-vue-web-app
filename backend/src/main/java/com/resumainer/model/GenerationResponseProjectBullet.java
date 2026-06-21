package com.resumainer.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Ordered bullet point for a generated project entry.
 * Maps to 'generation_response_project_bullet' table (BIGSERIAL PK).
 */
public class GenerationResponseProjectBullet {

    private long id;
    private UUID projectId;
    private int bulletOrder;
    private String bulletText;
    private boolean isEdited;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public GenerationResponseProjectBullet() {
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

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

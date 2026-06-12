package com.resumainer.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AI provider model configuration.
 * Maps to 'ai_model' table (UUID PK).
 * API key is encrypted at rest, masked in UI, never logged.
 */
public class AiModel {

    private UUID id;
    private String provider;
    private String modelCode;
    private String displayName;
    private String providerApiUrl;
    private String apiKeyEncrypted;

    private boolean isActive;
    private boolean isPaid;
    private boolean isHidden;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AiModel() {
    }

    // --- Getters and Setters ---

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getModelCode() { return modelCode; }
    public void setModelCode(String modelCode) { this.modelCode = modelCode; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getProviderApiUrl() { return providerApiUrl; }
    public void setProviderApiUrl(String providerApiUrl) { this.providerApiUrl = providerApiUrl; }

    public String getApiKeyEncrypted() { return apiKeyEncrypted; }
    public void setApiKeyEncrypted(String apiKeyEncrypted) { this.apiKeyEncrypted = apiKeyEncrypted; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }

    public boolean isHidden() { return isHidden; }
    public void setHidden(boolean hidden) { isHidden = hidden; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

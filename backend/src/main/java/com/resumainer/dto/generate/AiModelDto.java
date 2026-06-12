package com.resumainer.dto.generate;

import java.util.UUID;

/**
 * Safe AI model DTO returned to frontend.
 * Contains only display metadata — NEVER includes apiKeyEncrypted.
 */
public class AiModelDto {

    private UUID id;
    private String provider;
    private String displayName;
    private String modelCode;

    public AiModelDto() {}

    public AiModelDto(UUID id, String provider, String displayName, String modelCode) {
        this.id = id;
        this.provider = provider;
        this.displayName = displayName;
        this.modelCode = modelCode;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getModelCode() { return modelCode; }
    public void setModelCode(String modelCode) { this.modelCode = modelCode; }
}

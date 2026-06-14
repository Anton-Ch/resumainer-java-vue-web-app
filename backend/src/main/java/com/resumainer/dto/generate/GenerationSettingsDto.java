package com.resumainer.dto.generate;

import java.util.UUID;

/**
 * Optional settings override for the generate endpoint.
 * Frontend passes these when the user changed settings after request creation.
 * All fields are nullable — null means "keep existing value".
 */
public class GenerationSettingsDto {

    private String languageMode;         // ENGLISH_ONLY, RUSSIAN_ONLY, BILINGUAL
    private String adaptationSelection;  // MINIMAL, BALANCED, MAXIMUM, ALL
    private UUID aiModelId;
    private Boolean includeCoverLetter;

    public GenerationSettingsDto() {}

    public String getLanguageMode() { return languageMode; }
    public void setLanguageMode(String languageMode) { this.languageMode = languageMode; }

    public String getAdaptationSelection() { return adaptationSelection; }
    public void setAdaptationSelection(String adaptationSelection) { this.adaptationSelection = adaptationSelection; }

    public UUID getAiModelId() { return aiModelId; }
    public void setAiModelId(UUID aiModelId) { this.aiModelId = aiModelId; }

    public Boolean getIncludeCoverLetter() { return includeCoverLetter; }
    public void setIncludeCoverLetter(Boolean includeCoverLetter) { this.includeCoverLetter = includeCoverLetter; }
}

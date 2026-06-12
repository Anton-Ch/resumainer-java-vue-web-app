package com.resumainer.dto.generate;

import java.util.UUID;

/**
 * Request to create a new generation request (vacancy + settings).
 */
public class GenerationRequestCreateDto {

    private String vacancyTitle;
    private String vacancyDescription;
    private String companyName;
    private String companyDescription;
    private String additionalComments;
    private boolean includeCoverLetter;

    private String languageMode;         // ENGLISH_ONLY, RUSSIAN_ONLY, BILINGUAL
    private String adaptationSelection;  // MINIMAL, BALANCED, MAXIMUM, ALL
    private UUID aiModelId;              // selected AI model

    public GenerationRequestCreateDto() {}

    public String getVacancyTitle() { return vacancyTitle; }
    public void setVacancyTitle(String vacancyTitle) { this.vacancyTitle = vacancyTitle; }

    public String getVacancyDescription() { return vacancyDescription; }
    public void setVacancyDescription(String vacancyDescription) { this.vacancyDescription = vacancyDescription; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyDescription() { return companyDescription; }
    public void setCompanyDescription(String companyDescription) { this.companyDescription = companyDescription; }

    public String getAdditionalComments() { return additionalComments; }
    public void setAdditionalComments(String additionalComments) { this.additionalComments = additionalComments; }

    public boolean isIncludeCoverLetter() { return includeCoverLetter; }
    public void setIncludeCoverLetter(boolean includeCoverLetter) { this.includeCoverLetter = includeCoverLetter; }

    public String getLanguageMode() { return languageMode; }
    public void setLanguageMode(String languageMode) { this.languageMode = languageMode; }

    public String getAdaptationSelection() { return adaptationSelection; }
    public void setAdaptationSelection(String adaptationSelection) { this.adaptationSelection = adaptationSelection; }

    public UUID getAiModelId() { return aiModelId; }
    public void setAiModelId(UUID aiModelId) { this.aiModelId = aiModelId; }
}

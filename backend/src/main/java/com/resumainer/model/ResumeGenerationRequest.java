package com.resumainer.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AI generation request — captures user input (vacancy/company details),
 * generation settings (language mode, adaptation, AI model), and processing state.
 * Maps to the 'resume_generation_request' table (UUID PK).
 */
public class ResumeGenerationRequest {

    private UUID id;
    private UUID userId;
    private UUID aiModelId;

    private String vacancyTitle;
    private String vacancyDescription;
    private String companyName;
    private String companyDescription;
    private String additionalComments;
    private boolean includeCoverLetter;

    private String languageMode;       // ENGLISH_ONLY, RUSSIAN_ONLY, BILINGUAL
    private String adaptationSelection; // MINIMAL, BALANCED, MAXIMUM, ALL

    private UUID promptConfigId;
    private Long budgetConfigId;
    private Integer budgetConfigVersionUsed;

    private String status;             // pending, processing, completed, failed
    private String errorMessage;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public ResumeGenerationRequest() {
    }

    // --- Getters and Setters ---

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getAiModelId() { return aiModelId; }
    public void setAiModelId(UUID aiModelId) { this.aiModelId = aiModelId; }

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

    public UUID getPromptConfigId() { return promptConfigId; }
    public void setPromptConfigId(UUID promptConfigId) { this.promptConfigId = promptConfigId; }

    public Long getBudgetConfigId() { return budgetConfigId; }
    public void setBudgetConfigId(Long budgetConfigId) { this.budgetConfigId = budgetConfigId; }

    public Integer getBudgetConfigVersionUsed() { return budgetConfigVersionUsed; }
    public void setBudgetConfigVersionUsed(Integer budgetConfigVersionUsed) { this.budgetConfigVersionUsed = budgetConfigVersionUsed; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}

package com.resumainer.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Extended profile settings — mapped to the 'additional_profile_info' table (BIGSERIAL PK).
 * One-to-one relationship with User. Contains resume language preferences,
 * work format preferences, skills, languages, aspirations, and personal info.
 * No soft-delete — updated in-place or created on first save.
 */
public class AdditionalProfileInfo {

    private Long id;
    private UUID userId;

    private String skills;
    private String languages;
    private String professionalAspirations;
    private String achievements;
    private String generalInformation;

    private Long defaultResumeLanguageId;
    private Long additionalResumeLanguageId;

    private String readyForRelocation;
    private String readyForBusinessTrips;

    private LocalDate dateOfBirth;
    private String citizenship;

    private String photoFilePath;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AdditionalProfileInfo() {
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public String getProfessionalAspirations() {
        return professionalAspirations;
    }

    public void setProfessionalAspirations(String professionalAspirations) {
        this.professionalAspirations = professionalAspirations;
    }

    public String getAchievements() {
        return achievements;
    }

    public void setAchievements(String achievements) {
        this.achievements = achievements;
    }

    public String getGeneralInformation() {
        return generalInformation;
    }

    public void setGeneralInformation(String generalInformation) {
        this.generalInformation = generalInformation;
    }

    public Long getDefaultResumeLanguageId() {
        return defaultResumeLanguageId;
    }

    public void setDefaultResumeLanguageId(Long defaultResumeLanguageId) {
        this.defaultResumeLanguageId = defaultResumeLanguageId;
    }

    public Long getAdditionalResumeLanguageId() {
        return additionalResumeLanguageId;
    }

    public void setAdditionalResumeLanguageId(Long additionalResumeLanguageId) {
        this.additionalResumeLanguageId = additionalResumeLanguageId;
    }

    public String getReadyForRelocation() {
        return readyForRelocation;
    }

    public void setReadyForRelocation(String readyForRelocation) {
        this.readyForRelocation = readyForRelocation;
    }

    public String getReadyForBusinessTrips() {
        return readyForBusinessTrips;
    }

    public void setReadyForBusinessTrips(String readyForBusinessTrips) {
        this.readyForBusinessTrips = readyForBusinessTrips;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getCitizenship() {
        return citizenship;
    }

    public void setCitizenship(String citizenship) {
        this.citizenship = citizenship;
    }

    public String getPhotoFilePath() {
        return photoFilePath;
    }

    public void setPhotoFilePath(String photoFilePath) {
        this.photoFilePath = photoFilePath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdditionalProfileInfo that = (AdditionalProfileInfo) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AdditionalProfileInfo{id=" + id + ", userId=" + userId + "}";
    }
}

package com.resumainer.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Generated Personal Information per response language/adaptation.
 * Editable before final save. One row per response.
 * Maps to 'generation_response_personal' table (UUID PK).
 */
public class GenerationResponsePersonal {

    private UUID id;
    private UUID responseId;

    private String location;
    private String spokenLanguages;
    private String willingnessToRelocate;
    private String willingnessForBusinessTrips;
    private String citizenship;
    private LocalDate dateOfBirth;
    private String workFormats;
    private String gpaGrade;
    private int orderInResume;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public GenerationResponsePersonal() {
    }

    // --- Getters and Setters ---

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getResponseId() { return responseId; }
    public void setResponseId(UUID responseId) { this.responseId = responseId; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getSpokenLanguages() { return spokenLanguages; }
    public void setSpokenLanguages(String spokenLanguages) { this.spokenLanguages = spokenLanguages; }

    public String getWillingnessToRelocate() { return willingnessToRelocate; }
    public void setWillingnessToRelocate(String willingnessToRelocate) { this.willingnessToRelocate = willingnessToRelocate; }

    public String getWillingnessForBusinessTrips() { return willingnessForBusinessTrips; }
    public void setWillingnessForBusinessTrips(String willingnessForBusinessTrips) { this.willingnessForBusinessTrips = willingnessForBusinessTrips; }

    public String getCitizenship() { return citizenship; }
    public void setCitizenship(String citizenship) { this.citizenship = citizenship; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getWorkFormats() { return workFormats; }
    public void setWorkFormats(String workFormats) { this.workFormats = workFormats; }

    public String getGpaGrade() { return gpaGrade; }
    public void setGpaGrade(String gpaGrade) { this.gpaGrade = gpaGrade; }

    public int getOrderInResume() { return orderInResume; }
    public void setOrderInResume(int orderInResume) { this.orderInResume = orderInResume; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

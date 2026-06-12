package com.resumainer.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Formal education record — mapped to the 'education' table (BIGSERIAL PK).
 * Stores universities, colleges, degrees, and programs.
 * Supports soft-delete per SEC-003.
 */
public class Education {

    private Long id;
    private UUID userId;

    private String institutionNameRu;
    private String institutionNameEn;
    private String degreeRu;
    private String degreeEn;
    private String fieldOfStudyRu;
    private String fieldOfStudyEn;
    private String description;

    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isCurrent;

    private String location;
    private String gpaGrade;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
    private LocalDateTime deletedAt;

    public Education() {
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

    // --- Bilingual fields (DEC-070) ---

    public String getInstitutionNameRu() {
        return institutionNameRu;
    }

    public void setInstitutionNameRu(String institutionNameRu) {
        this.institutionNameRu = institutionNameRu;
    }

    public String getInstitutionNameEn() {
        return institutionNameEn;
    }

    public void setInstitutionNameEn(String institutionNameEn) {
        this.institutionNameEn = institutionNameEn;
    }

    public String getDegreeRu() {
        return degreeRu;
    }

    public void setDegreeRu(String degreeRu) {
        this.degreeRu = degreeRu;
    }

    public String getDegreeEn() {
        return degreeEn;
    }

    public void setDegreeEn(String degreeEn) {
        this.degreeEn = degreeEn;
    }

    public String getFieldOfStudyRu() {
        return fieldOfStudyRu;
    }

    public void setFieldOfStudyRu(String fieldOfStudyRu) {
        this.fieldOfStudyRu = fieldOfStudyRu;
    }

    public String getFieldOfStudyEn() {
        return fieldOfStudyEn;
    }

    public void setFieldOfStudyEn(String fieldOfStudyEn) {
        this.fieldOfStudyEn = fieldOfStudyEn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent = current;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getGpaGrade() {
        return gpaGrade;
    }

    public void setGpaGrade(String gpaGrade) {
        this.gpaGrade = gpaGrade;
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

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Education education = (Education) o;
        return Objects.equals(id, education.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Education{id=" + id + ", userId=" + userId + ", institutionNameEn='" + institutionNameEn + "'}";
    }
}

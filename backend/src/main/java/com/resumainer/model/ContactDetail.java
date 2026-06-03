package com.resumainer.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * User profile contact information — mapped to the 'contact_detail' table (UUID PK).
 * One-to-one relationship with User.
 * Created as empty shell on registration, filled later in My Profile.
 */
public class ContactDetail {

    private UUID id;
    private UUID userId;
    private String fullName;
    private String phone;
    private String resumeEmail;
    private String location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ContactDetail() {
    }

    // --- Factory for creating empty profile shell ---

    /**
     * Creates an empty ContactDetail shell linked to a user, used on registration.
     */
    public static ContactDetail createEmpty(UUID userId) {
        ContactDetail cd = new ContactDetail();
        cd.setUserId(userId);
        return cd;
    }

    // --- Getters and Setters ---

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getResumeEmail() {
        return resumeEmail;
    }

    public void setResumeEmail(String resumeEmail) {
        this.resumeEmail = resumeEmail;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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
        ContactDetail that = (ContactDetail) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ContactDetail{id=" + id + ", userId=" + userId + "}";
    }
}

package com.resumainer.dto;

import java.util.Map;

/**
 * DTO for profile section completion status shown in the sidebar navigation.
 * Each section returns either "completed"/"incomplete" or a record count.
 */
public class ProfileSectionStatus {

    private Object contact;
    private Map<String, Object> experience;
    private Map<String, Object> education;
    private Map<String, Object> projects;
    private Map<String, Object> courses;
    private Object additional;

    public ProfileSectionStatus() {
    }

    public Object getContact() {
        return contact;
    }

    public void setContact(Object contact) {
        this.contact = contact;
    }

    public Map<String, Object> getExperience() {
        return experience;
    }

    public void setExperience(Map<String, Object> experience) {
        this.experience = experience;
    }

    public Map<String, Object> getEducation() {
        return education;
    }

    public void setEducation(Map<String, Object> education) {
        this.education = education;
    }

    public Map<String, Object> getProjects() {
        return projects;
    }

    public void setProjects(Map<String, Object> projects) {
        this.projects = projects;
    }

    public Map<String, Object> getCourses() {
        return courses;
    }

    public void setCourses(Map<String, Object> courses) {
        this.courses = courses;
    }

    public Object getAdditional() {
        return additional;
    }

    public void setAdditional(Object additional) {
        this.additional = additional;
    }
}

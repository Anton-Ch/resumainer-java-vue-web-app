package com.resumainer.model.pdf;

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable render input for the PDF/HTML generation pipeline.
 * Assembled from profile data and generated response data before rendering.
 * Ported from spike V12.1 ResumeData record, adapted for production data sources.
 */
public class ResumeRenderData {

    private String languageCode;       // EN or RU
    private String fullName;
    private String professionalTitle;
    private String phone;
    private String email;
    private String location;
    private String linkedin;
    private String portfolio;
    private String telegram;
    private String whatsapp;
    private String valueLine;
    private String professionalSummary;
    private String professionalAspirations;
    private String personalLine1;
    private String personalLine2;
    private String personalLine3;
    private String coverLetter;

    private List<RenderWorkItem> workExperience = new ArrayList<>();
    private List<RenderProjectItem> projects = new ArrayList<>();
    private List<RenderCourseItem> courses = new ArrayList<>();
    private List<RenderSkillGroup> skills = new ArrayList<>();
    private List<String> education = new ArrayList<>();

    public ResumeRenderData() {}

    // --- Simple getters/setters ---
    public String getLanguageCode() { return languageCode; }
    public void setLanguageCode(String languageCode) { this.languageCode = languageCode; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getProfessionalTitle() { return professionalTitle; }
    public void setProfessionalTitle(String professionalTitle) { this.professionalTitle = professionalTitle; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getLinkedin() { return linkedin; }
    public void setLinkedin(String linkedin) { this.linkedin = linkedin; }
    public String getPortfolio() { return portfolio; }
    public void setPortfolio(String portfolio) { this.portfolio = portfolio; }
    public String getTelegram() { return telegram; }
    public void setTelegram(String telegram) { this.telegram = telegram; }
    public String getWhatsapp() { return whatsapp; }
    public void setWhatsapp(String whatsapp) { this.whatsapp = whatsapp; }
    public String getValueLine() { return valueLine; }
    public void setValueLine(String valueLine) { this.valueLine = valueLine; }
    public String getProfessionalSummary() { return professionalSummary; }
    public void setProfessionalSummary(String professionalSummary) { this.professionalSummary = professionalSummary; }
    public String getProfessionalAspirations() { return professionalAspirations; }
    public void setProfessionalAspirations(String professionalAspirations) { this.professionalAspirations = professionalAspirations; }
    public String getPersonalLine1() { return personalLine1; }
    public void setPersonalLine1(String personalLine1) { this.personalLine1 = personalLine1; }
    public String getPersonalLine2() { return personalLine2; }
    public void setPersonalLine2(String personalLine2) { this.personalLine2 = personalLine2; }
    public String getPersonalLine3() { return personalLine3; }
    public void setPersonalLine3(String personalLine3) { this.personalLine3 = personalLine3; }
    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }
    public List<RenderWorkItem> getWorkExperience() { return workExperience; }
    public void setWorkExperience(List<RenderWorkItem> workExperience) { this.workExperience = workExperience; }
    public List<RenderProjectItem> getProjects() { return projects; }
    public void setProjects(List<RenderProjectItem> projects) { this.projects = projects; }
    public List<RenderCourseItem> getCourses() { return courses; }
    public void setCourses(List<RenderCourseItem> courses) { this.courses = courses; }
    public List<RenderSkillGroup> getSkills() { return skills; }
    public void setSkills(List<RenderSkillGroup> skills) { this.skills = skills; }
    public List<String> getEducation() { return education; }
    public void setEducation(List<String> education) { this.education = education; }

    // --- Inner types for render-specific structures ---

    public static class RenderWorkItem {
        private String jobTitle;
        private String companyName;
        private String description;
        private String location;
        private String dateRange;
        private List<String> bulletPoints = new ArrayList<>();
        private boolean isFirstPage;

        public String getJobTitle() { return jobTitle; }
        public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getDateRange() { return dateRange; }
        public void setDateRange(String dateRange) { this.dateRange = dateRange; }
        public List<String> getBulletPoints() { return bulletPoints; }
        public void setBulletPoints(List<String> bulletPoints) { this.bulletPoints = bulletPoints; }
        public boolean isFirstPage() { return isFirstPage; }
        public void setFirstPage(boolean firstPage) { isFirstPage = firstPage; }
    }

    public static class RenderProjectItem {
        private String projectName;
        private String role;
        private String description;
        private String dateRange;
        private List<String> bulletPoints = new ArrayList<>();

        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getDateRange() { return dateRange; }
        public void setDateRange(String dateRange) { this.dateRange = dateRange; }
        public List<String> getBulletPoints() { return bulletPoints; }
        public void setBulletPoints(List<String> bulletPoints) { this.bulletPoints = bulletPoints; }
    }

    public static class RenderCourseItem {
        private String name;
        private String provider;
        private String courseFocus;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }
        public String getCourseFocus() { return courseFocus; }
        public void setCourseFocus(String courseFocus) { this.courseFocus = courseFocus; }
    }

    public static class RenderSkillGroup {
        private String groupName;
        private List<String> skills = new ArrayList<>();

        public String getGroupName() { return groupName; }
        public void setGroupName(String groupName) { this.groupName = groupName; }
        public List<String> getSkills() { return skills; }
        public void setSkills(List<String> skills) { this.skills = skills; }
    }
}

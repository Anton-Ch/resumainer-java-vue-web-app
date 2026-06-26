package com.resumainer.dto.admin;

/**
 * Read-only additional info section for admin user details.
 * Maps to additional_profile_info table.
 * Does NOT expose photo_file_path or sensitive fields.
 */
public class AdminUserAdditionalInfoDto {

    private String skills;
    private String languages;
    private String professionalAspirations;
    private String achievements;
    private String generalInformation;
    private String readyForRelocation;
    private String readyForBusinessTrips;
    private String dateOfBirth;
    private String citizenship;

    public AdminUserAdditionalInfoDto() {
    }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getLanguages() { return languages; }
    public void setLanguages(String languages) { this.languages = languages; }

    public String getProfessionalAspirations() { return professionalAspirations; }
    public void setProfessionalAspirations(String professionalAspirations) { this.professionalAspirations = professionalAspirations; }

    public String getAchievements() { return achievements; }
    public void setAchievements(String achievements) { this.achievements = achievements; }

    public String getGeneralInformation() { return generalInformation; }
    public void setGeneralInformation(String generalInformation) { this.generalInformation = generalInformation; }

    public String getReadyForRelocation() { return readyForRelocation; }
    public void setReadyForRelocation(String readyForRelocation) { this.readyForRelocation = readyForRelocation; }

    public String getReadyForBusinessTrips() { return readyForBusinessTrips; }
    public void setReadyForBusinessTrips(String readyForBusinessTrips) { this.readyForBusinessTrips = readyForBusinessTrips; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getCitizenship() { return citizenship; }
    public void setCitizenship(String citizenship) { this.citizenship = citizenship; }
}

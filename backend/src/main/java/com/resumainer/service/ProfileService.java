package com.resumainer.service;

import com.resumainer.dao.*;
import com.resumainer.dto.CoursePage;
import com.resumainer.dto.ProfileSectionStatus;
import com.resumainer.exception.ServiceException;
import com.resumainer.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * Service for user profile CRUD operations across all 6 profile sections.
 * <p>
 * Manages business logic, owner-scoped access (SEC-001), JDBC transactions
 * for atomic multi-table operations, and PII-safe logging (SEC-002).
 * No profile field values are logged — only operation type, user ID, and status.
 */
@Service
public class ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);

    private final DataSource dataSource;
    private final ContactDetailDao contactDetailDao;
    private final WorkExperienceDao workExperienceDao;
    private final EducationDao educationDao;
    private final ProjectDao projectDao;
    private final CourseCertificateDao courseCertificateDao;
    private final AdditionalProfileInfoDao additionalProfileInfoDao;
    private final WorkFormatDao workFormatDao;
    private final UserDao userDao;

    public ProfileService(DataSource dataSource,
                          ContactDetailDao contactDetailDao,
                          WorkExperienceDao workExperienceDao,
                          EducationDao educationDao,
                          ProjectDao projectDao,
                          CourseCertificateDao courseCertificateDao,
                          AdditionalProfileInfoDao additionalProfileInfoDao,
                          WorkFormatDao workFormatDao,
                          UserDao userDao) {
        this.dataSource = dataSource;
        this.contactDetailDao = contactDetailDao;
        this.workExperienceDao = workExperienceDao;
        this.educationDao = educationDao;
        this.projectDao = projectDao;
        this.courseCertificateDao = courseCertificateDao;
        this.additionalProfileInfoDao = additionalProfileInfoDao;
        this.workFormatDao = workFormatDao;
        this.userDao = userDao;
    }

    // ========================================================================
    // Section Status
    // ========================================================================

    /**
     * Returns completion status for all 6 profile sections.
     * Used by the sidebar navigation to show completed/incomplete indicators.
     */
    public ProfileSectionStatus getSectionStatus(UUID userId) {
        log.debug("getSectionStatus: userId={}", userId);

        ProfileSectionStatus status = new ProfileSectionStatus();

        // Contact: completed when all required fields are non-empty
        ContactDetail contact = contactDetailDao.findByUserId(userId);
        boolean contactComplete = contact != null
                && isNotBlank(contact.getFullName())
                && isNotBlank(contact.getProfessionalTitle())
                && isNotBlank(contact.getResumeEmail())
                && isNotBlank(contact.getPhone())
                && isNotBlank(contact.getLocation());
        status.setContact(contactComplete ? "completed" : "incomplete");

        // Experience: record count
        List<WorkExperience> experiences = workExperienceDao.findByUserId(userId);
        status.setExperience(recordCount(experiences.size()));

        // Education: record count
        List<Education> educations = educationDao.findByUserId(userId);
        status.setEducation(recordCount(educations.size()));

        // Projects: record count
        List<Project> projects = projectDao.findByUserId(userId);
        status.setProjects(recordCount(projects.size()));

        // Courses: record count
        long courseCount = courseCertificateDao.countByUserId(userId, null, null, null);
        status.setCourses(recordCount((int) courseCount));

        // Additional: completed when required fields are non-empty
        AdditionalProfileInfo additional = additionalProfileInfoDao.findByUserId(userId);
        boolean additionalComplete = additional != null
                && isNotBlank(additional.getCitizenship())
                && additional.getDateOfBirth() != null;
        status.setAdditional(additionalComplete ? "completed" : "incomplete");

        return status;
    }

    // ========================================================================
    // Contact Details
    // ========================================================================

    public ContactDetail getContactDetails(UUID userId) {
        log.debug("getContactDetails: userId={}", userId);
        return contactDetailDao.findByUserId(userId);
    }

    public ContactDetail updateContactDetails(UUID userId, ContactDetail contact) {
        log.debug("updateContactDetails: userId={}", userId);

        ContactDetail existing = contactDetailDao.findByUserId(userId);
        if (existing == null) {
            throw new ServiceException("profile.contact.notFound", "Contact details not found");
        }

        contact.setUserId(userId);
        contactDetailDao.update(contact);
        log.info("Contact details updated: userId={}", userId);
        return contactDetailDao.findByUserId(userId);
    }

    // ========================================================================
    // Work Experience
    // ========================================================================

    public List<WorkExperience> getWorkExperiences(UUID userId) {
        log.debug("getWorkExperiences: userId={}", userId);
        return workExperienceDao.findByUserId(userId);
    }

    public WorkExperience createWorkExperience(UUID userId, WorkExperience experience) {
        log.debug("createWorkExperience: userId={}", userId);
        experience.setUserId(userId);
        WorkExperience created = workExperienceDao.create(experience);
        log.info("Work experience created: id={}, userId={}", created.getId(), userId);
        return created;
    }

    public void updateWorkExperience(UUID userId, long id, WorkExperience experience) {
        log.debug("updateWorkExperience: id={}, userId={}", id, userId);
        experience.setId(id);
        experience.setUserId(userId);
        workExperienceDao.update(experience);
        log.info("Work experience updated: id={}, userId={}", id, userId);
    }

    public boolean deleteWorkExperience(UUID userId, long id) {
        log.debug("deleteWorkExperience: id={}, userId={}", id, userId);
        boolean deleted = workExperienceDao.softDelete(id, userId);
        if (deleted) {
            log.info("Work experience deleted: id={}, userId={}", id, userId);
        }
        return deleted;
    }

    // ========================================================================
    // Education
    // ========================================================================

    public List<Education> getEducations(UUID userId) {
        log.debug("getEducations: userId={}", userId);
        return educationDao.findByUserId(userId);
    }

    public Education createEducation(UUID userId, Education education) {
        log.debug("createEducation: userId={}", userId);
        education.setUserId(userId);
        Education created = educationDao.create(education);
        log.info("Education created: id={}, userId={}", created.getId(), userId);
        return created;
    }

    public void updateEducation(UUID userId, long id, Education education) {
        log.debug("updateEducation: id={}, userId={}", id, userId);
        education.setId(id);
        education.setUserId(userId);
        educationDao.update(education);
        log.info("Education updated: id={}, userId={}", id, userId);
    }

    public boolean deleteEducation(UUID userId, long id) {
        log.debug("deleteEducation: id={}, userId={}", id, userId);
        boolean deleted = educationDao.softDelete(id, userId);
        if (deleted) {
            log.info("Education deleted: id={}, userId={}", id, userId);
        }
        return deleted;
    }

    // ========================================================================
    // Projects
    // ========================================================================

    public List<Project> getProjects(UUID userId) {
        log.debug("getProjects: userId={}", userId);
        return projectDao.findByUserId(userId);
    }

    public Project createProject(UUID userId, Project project) {
        log.debug("createProject: userId={}", userId);
        project.setUserId(userId);
        Project created = projectDao.create(project);
        log.info("Project created: id={}, userId={}", created.getId(), userId);
        return created;
    }

    public void updateProject(UUID userId, long id, Project project) {
        log.debug("updateProject: id={}, userId={}", id, userId);
        project.setId(id);
        project.setUserId(userId);
        projectDao.update(project);
        log.info("Project updated: id={}, userId={}", id, userId);
    }

    public boolean deleteProject(UUID userId, long id) {
        log.debug("deleteProject: id={}, userId={}", id, userId);
        boolean deleted = projectDao.softDelete(id, userId);
        if (deleted) {
            log.info("Project deleted: id={}, userId={}", id, userId);
        }
        return deleted;
    }

    // ========================================================================
    // Courses & Certificates
    // ========================================================================

    public CoursePage getCourses(UUID userId, int page, int size,
                                  String sortField, String sortDir,
                                  String search, String dateFrom, String dateTo) {
        log.debug("getCourses: userId={}, page={}, size={}", userId, page, size);

        // Validate size
        if (size <= 0 || size > 50) {
            size = 10;
        }
        Set<Integer> allowedSizes = Set.of(10, 20, 50);
        if (!allowedSizes.contains(size)) {
            size = 10;
        }

        List<CourseCertificate> courses = courseCertificateDao.findByUserId(
                userId, search, dateFrom, dateTo, sortField, sortDir, page, size);
        long total = courseCertificateDao.countByUserId(userId, search, dateFrom, dateTo);

        return new CoursePage(courses, total, page, size);
    }

    public CourseCertificate createCourse(UUID userId, CourseCertificate course) {
        log.debug("createCourse: userId={}", userId);
        course.setUserId(userId);
        CourseCertificate created = courseCertificateDao.create(course);
        log.info("Course created: id={}, userId={}", created.getId(), userId);
        return created;
    }

    public void updateCourse(UUID userId, long id, CourseCertificate course) {
        log.debug("updateCourse: id={}, userId={}", id, userId);
        course.setId(id);
        course.setUserId(userId);
        courseCertificateDao.update(course);
        log.info("Course updated: id={}, userId={}", id, userId);
    }

    public boolean deleteCourse(UUID userId, long id) {
        log.debug("deleteCourse: id={}, userId={}", id, userId);
        boolean deleted = courseCertificateDao.softDelete(id, userId);
        if (deleted) {
            log.info("Course deleted: id={}, userId={}", id, userId);
        }
        return deleted;
    }

    // ========================================================================
    // Additional Info
    // ========================================================================

    /**
     * Returns aggregated additional info including work formats.
     */
    public Map<String, Object> getAdditionalInfo(UUID userId) {
        log.debug("getAdditionalInfo: userId={}", userId);

        AdditionalProfileInfo info = additionalProfileInfoDao.findByUserId(userId);
        User user = userDao.findById(userId);
        List<WorkFormat> workFormats = workFormatDao.findByUserId(userId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("username", user != null ? user.getUsername() : null);
        result.put("defaultResumeLanguage",
                info != null ? info.getDefaultResumeLanguageId() : null);
        result.put("additionalResumeLanguage",
                info != null ? info.getAdditionalResumeLanguageId() : null);
        result.put("acceptableWorkFormats",
                workFormats.stream().map(WorkFormat::getCode).toList());
        result.put("willingnessToRelocate",
                info != null ? info.getReadyForRelocation() : null);
        result.put("willingnessForBusinessTravel",
                info != null ? info.getReadyForBusinessTrips() : null);
        result.put("skills", info != null ? info.getSkills() : null);
        result.put("spokenLanguages", info != null ? info.getLanguages() : null);
        result.put("professionalAspirations",
                info != null ? info.getProfessionalAspirations() : null);
        result.put("achievements", info != null ? info.getAchievements() : null);
        result.put("additionalContextForAI",
                info != null ? info.getGeneralInformation() : null);
        result.put("dateOfBirth", info != null ? info.getDateOfBirth() : null);
        result.put("citizenship", info != null ? info.getCitizenship() : null);

        return result;
    }

    /**
     * Saves additional info + username + work formats in a single transaction.
     */
    public void updateAdditionalInfo(UUID userId, Map<String, Object> data) {
        log.debug("updateAdditionalInfo: userId={}", userId);

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            // Update username in users table
            String username = (String) data.get("username");
            if (username != null) {
                userDao.updateUsername(userId, username, conn);
            }

            // Upsert additional_profile_info
            AdditionalProfileInfo info = mapToAdditionalInfo(userId, data);
            additionalProfileInfoDao.upsert(info, conn);

            // Save work formats
            @SuppressWarnings("unchecked")
            List<String> formatCodes = (List<String>) data.get("acceptableWorkFormats");
            if (formatCodes != null) {
                List<Long> formatIds = resolveWorkFormatIds(formatCodes);
                workFormatDao.saveUserFormats(userId, formatIds, conn);
            }

            conn.commit();
            log.info("Additional info updated: userId={}", userId);

        } catch (Exception e) {
            rollbackQuietly(conn);
            log.error("Failed to update additional info (rollback): userId={}", userId, e);
            throw new ServiceException("profile.additional.saveFailed",
                    "Failed to save additional profile information", e);
        } finally {
            closeQuietly(conn);
        }
    }

    // ========================================================================
    // Work Format helpers
    // ========================================================================

    public List<WorkFormat> getAllWorkFormats() {
        return workFormatDao.findAll();
    }

    // ========================================================================
    // Private helpers
    // ========================================================================

    private Map<String, Object> recordCount(int count) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("count", count);
        m.put("label", count + " records");
        return m;
    }

    private boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }

    private AdditionalProfileInfo mapToAdditionalInfo(UUID userId, Map<String, Object> data) {
        AdditionalProfileInfo info = new AdditionalProfileInfo();
        info.setUserId(userId);
        info.setSkills(nullIfBlank((String) data.get("skills")));
        info.setLanguages(nullIfBlank((String) data.get("spokenLanguages")));
        info.setProfessionalAspirations(nullIfBlank((String) data.get("professionalAspirations")));
        info.setAchievements(nullIfBlank((String) data.get("achievements")));
        info.setGeneralInformation(nullIfBlank((String) data.get("additionalContextForAI")));

        Number defaultLang = (Number) data.get("defaultResumeLanguage");
        if (defaultLang != null) info.setDefaultResumeLanguageId(defaultLang.longValue());
        Number additionalLang = (Number) data.get("additionalResumeLanguage");
        if (additionalLang != null) info.setAdditionalResumeLanguageId(additionalLang.longValue());

        info.setReadyForRelocation(nullIfBlank((String) data.get("willingnessToRelocate")));
        info.setReadyForBusinessTrips(nullIfBlank((String) data.get("willingnessForBusinessTravel")));

        // Date of birth and citizenship are required
        String dobStr = (String) data.get("dateOfBirth");
        if (dobStr != null && !dobStr.isBlank()) {
            info.setDateOfBirth(java.time.LocalDate.parse(dobStr));
        }
        info.setCitizenship((String) data.get("citizenship"));

        return info;
    }

    private List<Long> resolveWorkFormatIds(List<String> codes) {
        List<WorkFormat> all = workFormatDao.findAll();
        Map<String, Long> codeToId = new HashMap<>();
        for (WorkFormat wf : all) {
            codeToId.put(wf.getCode(), wf.getId());
        }
        List<Long> ids = new ArrayList<>();
        for (String code : codes) {
            Long id = codeToId.get(code);
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }

    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                log.warn("Rollback failed", e);
            }
        }
    }

    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.warn("Failed to close connection", e);
            }
        }
    }

    /**
     * Converts a blank string to null to avoid CHECK constraint violations
     * and unnecessary empty string storage in optional fields.
     */
    private String nullIfBlank(String value) {
        return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
    }
}

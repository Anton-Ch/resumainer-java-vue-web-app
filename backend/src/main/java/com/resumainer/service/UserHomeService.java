package com.resumainer.service;

import com.resumainer.dao.AdditionalProfileInfoDao;
import com.resumainer.dao.ContactDetailDao;
import com.resumainer.dao.EducationDao;
import com.resumainer.dao.ResumeDao;
import com.resumainer.dao.WorkExperienceDao;
import com.resumainer.dto.home.HomeSavedResumeDto;
import com.resumainer.model.AdditionalProfileInfo;
import com.resumainer.model.SavedResume;
import com.resumainer.model.UserHomeSummary;
import com.resumainer.model.UserHomeSummary.ProfileChecklist;
import com.resumainer.model.UserHomeSummary.Summary;
import com.resumainer.util.ProfileReadinessCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

/**
 * Service for the User Home summary endpoint.
 * <p>
 * Composes profile readiness, checklist status, resume counts,
 * and last resume preview from multiple DAOs.
 */
@Service
public class UserHomeService {

    private static final Logger log = LoggerFactory.getLogger(UserHomeService.class);

    private final ContactDetailDao contactDetailDao;
    private final ResumeDao resumeDao;
    private final WorkExperienceDao workExperienceDao;
    private final EducationDao educationDao;
    private final AdditionalProfileInfoDao additionalProfileInfoDao;
    private final HomeSavedResumeMapper homeMapper;

    public UserHomeService(ContactDetailDao contactDetailDao, ResumeDao resumeDao,
                           WorkExperienceDao workExperienceDao, EducationDao educationDao,
                           AdditionalProfileInfoDao additionalProfileInfoDao,
                           HomeSavedResumeMapper homeMapper) {
        this.contactDetailDao = contactDetailDao;
        this.resumeDao = resumeDao;
        this.workExperienceDao = workExperienceDao;
        this.educationDao = educationDao;
        this.additionalProfileInfoDao = additionalProfileInfoDao;
        this.homeMapper = homeMapper;
    }

    /**
     * Build the home summary for a given user.
     *
     * @param userId  the authenticated user ID
     * @param request the HTTP request (for public URL resolution in mapped DTO)
     */
    public UserHomeSummary getHomeSummary(UUID userId, HttpServletRequest request) {
        log.debug("getHomeSummary: userId={}", userId);

        boolean contactComplete = isContactComplete(userId);
        boolean hasWorkExperience = hasWorkExperience(userId);
        boolean hasEducation = hasEducation(userId);
        boolean hasAdditionalInfo = hasAdditionalInfo(userId);

        ProfileChecklist checklist = ProfileReadinessCalculator.calculateChecklist(
                contactComplete, hasWorkExperience, hasEducation, hasAdditionalInfo);
        boolean profileReady = ProfileReadinessCalculator.isReady(checklist);

        List<SavedResume> firstPage = resumeDao.findByUserId(userId, null, null, null,
                null, null, null, "created_at", "desc", 0, 1);
        long totalResumes = resumeDao.countByUserId(userId, null, null, null, null, null, null);

        SavedResume lastResumeEntity = firstPage.isEmpty() ? null : firstPage.get(0);
        Long lastResumeId = lastResumeEntity != null ? lastResumeEntity.getId() : null;

        HomeSavedResumeDto lastResumeDto = lastResumeEntity != null
                ? homeMapper.toDto(lastResumeEntity, request)
                : null;

        Summary summary = new Summary(totalResumes,
                profileReady ? "READY" : "INCOMPLETE",
                lastResumeId);
        summary.setLastResume(lastResumeDto);

        UserHomeSummary result = new UserHomeSummary();
        result.setProfileReady(profileReady);
        result.setProfileChecklist(checklist);
        result.setSummary(summary);
        result.setLastResume(lastResumeDto);

        return result;
    }

    private boolean isContactComplete(UUID userId) {
        try {
            var contactDetail = contactDetailDao.findByUserId(userId);
            if (contactDetail == null) return false;
            return isNotBlank(contactDetail.getFullName())
                    && isNotBlank(contactDetail.getResumeEmail())
                    && isNotBlank(contactDetail.getPhone())
                    && isNotBlank(contactDetail.getLocation());
        } catch (Exception e) {
            log.warn("Could not check contact completeness for user {}: {}", userId, e.getMessage());
            return false;
        }
    }

    private boolean hasWorkExperience(UUID userId) {
        try {
            return !workExperienceDao.findByUserId(userId).isEmpty();
        } catch (Exception e) {
            log.warn("Failed to check work experience for user: {}", userId, e);
            return false;
        }
    }

    private boolean hasEducation(UUID userId) {
        try {
            return !educationDao.findByUserId(userId).isEmpty();
        } catch (Exception e) {
            log.warn("Failed to check education for user: {}", userId, e);
            return false;
        }
    }

    private boolean hasAdditionalInfo(UUID userId) {
        try {
            AdditionalProfileInfo info = additionalProfileInfoDao.findByUserId(userId);
            if (info == null) return false;
            return info.getDateOfBirth() != null && isNotBlank(info.getCitizenship());
        } catch (Exception e) {
            log.warn("Failed to check additional info for user: {}", userId, e);
            return false;
        }
    }

    private boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }
}

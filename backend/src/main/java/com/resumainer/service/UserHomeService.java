package com.resumainer.service;

import com.resumainer.dao.ContactDetailDao;
import com.resumainer.dao.ResumeDao;
import com.resumainer.model.SavedResume;
import com.resumainer.model.UserHomeSummary;
import com.resumainer.model.UserHomeSummary.ProfileChecklist;
import com.resumainer.model.UserHomeSummary.Summary;
import com.resumainer.util.ProfileReadinessCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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

    public UserHomeService(ContactDetailDao contactDetailDao, ResumeDao resumeDao) {
        this.contactDetailDao = contactDetailDao;
        this.resumeDao = resumeDao;
    }

    /**
     * Build the home summary for a given user.
     */
    public UserHomeSummary getHomeSummary(UUID userId) {
        log.debug("getHomeSummary: userId={}", userId);

        boolean contactComplete = isContactComplete(userId);
        boolean hasWorkExperience = hasWorkExperience(userId);
        boolean hasEducation = hasEducation(userId);

        ProfileChecklist checklist = ProfileReadinessCalculator.calculateChecklist(
                contactComplete, hasWorkExperience, hasEducation);
        boolean profileReady = ProfileReadinessCalculator.isReady(checklist);

        List<SavedResume> firstPage = resumeDao.findByUserId(userId, null, null, null,
                null, null, null, "created_at", "desc", 0, 1);
        long totalResumes = resumeDao.countByUserId(userId, null, null, null, null, null, null);

        SavedResume lastResume = firstPage.isEmpty() ? null : firstPage.get(0);
        Long lastResumeId = lastResume != null ? lastResume.getId() : null;

        Summary summary = new Summary(totalResumes,
                profileReady ? "READY" : "INCOMPLETE",
                lastResumeId);

        UserHomeSummary result = new UserHomeSummary();
        result.setProfileReady(profileReady);
        result.setProfileChecklist(checklist);
        result.setSummary(summary);
        result.setLastResume(lastResume);

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
        return false; // TODO: implement when WorkExperience feature is built
    }

    private boolean hasEducation(UUID userId) {
        return false; // TODO: implement when Education feature is built
    }

    private boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }
}

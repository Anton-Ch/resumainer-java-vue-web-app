package com.resumainer.service;

import com.resumainer.dao.ResumeBudgetConfigDao;
import com.resumainer.dao.ResumeBudgetConfigDao.BudgetConfig;
import com.resumainer.dao.ResumeBudgetConfigDao.SectionBudget;
import com.resumainer.dao.ResumeBudgetConfigDao.WorkExperienceDistributionRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Thin service over ResumeBudgetConfigDao.
 * Reads active budget config and provides resolved budget values for prompt building.
 * No cache for MVP — every generation reads fresh from DB.
 * Follows DEC-007-BUDGET-001: budget values come from DB, not hardcoded.
 */
@Service
public class ResumeBudgetConfigService {

    private static final Logger log = LoggerFactory.getLogger(ResumeBudgetConfigService.class);

    private final ResumeBudgetConfigDao dao;

    /** Default profile keys used for MVP budget resolution. */
    private static final String SKILLS_PROFILE = "light";
    private static final String COURSES_PROFILE = "medium";
    private static final String PROJECTS_PROFILE = "light";

    public ResumeBudgetConfigService(ResumeBudgetConfigDao dao) {
        this.dao = dao;
    }

    /**
     * Loads the active budget config. Throws if none is active.
     */
    public BudgetConfig getActiveBudgetConfig() {
        BudgetConfig config = dao.loadActiveConfig();
        if (config == null) {
            throw new IllegalStateException("No active resume budget configuration found. "
                    + "Please contact an administrator.");
        }
        log.debug("Active budget config: id={}, name={}, version={}", config.id, config.name, config.versionNo);
        return config;
    }

    // ─── Skills budget methods ─────────────────────────────────────

    public int getSkillsGroups() {
        return getMin("skills", SKILLS_PROFILE, "groups");
    }

    public int getSkillsGroupsMax() {
        return getMax("skills", SKILLS_PROFILE, "groups");
    }

    public int getSkillsPerGroup() {
        return getMin("skills", SKILLS_PROFILE, "skills_per_group");
    }

    public int getSkillsPerGroupMax() {
        return getMax("skills", SKILLS_PROFILE, "skills_per_group");
    }

    public int getWordsPerSkill() {
        return getMin("skills", SKILLS_PROFILE, "words_per_skill");
    }

    public int getWordsPerSkillMax() {
        return getMax("skills", SKILLS_PROFILE, "words_per_skill");
    }

    // ─── Courses budget methods ────────────────────────────────────

    public int getMaxCourses() {
        return getMax("courses", COURSES_PROFILE, "max_courses");
    }

    public int getCourseFocusWordsMin() {
        return getMin("courses", COURSES_PROFILE, "focus_words_per_course");
    }

    public int getCourseFocusWordsMax() {
        return getMax("courses", COURSES_PROFILE, "focus_words_per_course");
    }

    // ─── Projects budget methods ───────────────────────────────────

    public int getMaxProjects() {
        return getMax("projects", PROJECTS_PROFILE, "max_projects");
    }

    public int getProjectSentencesMin() {
        return getMin("projects", PROJECTS_PROFILE, "sentences_per_project");
    }

    public int getProjectSentencesMax() {
        return getMax("projects", PROJECTS_PROFILE, "sentences_per_project");
    }

    public int getProjectBulletsMin() {
        return getMin("projects", PROJECTS_PROFILE, "bullet_points_per_project");
    }

    public int getProjectBulletsMax() {
        return getMax("projects", PROJECTS_PROFILE, "bullet_points_per_project");
    }

    // ─── Work Experience distribution methods ──────────────────────

    public List<WorkExperienceDistributionRule> getWorkExperienceDistributionRules() {
        BudgetConfig config = getActiveBudgetConfig();
        return dao.loadWorkExperienceDistributionRules(config.id);
    }

    // ─── Internal helpers ──────────────────────────────────────────

    private int getMin(String section, String profile, String metric) {
        BudgetConfig config = getActiveBudgetConfig();
        SectionBudget sb = dao.loadSectionBudget(config.id, section, profile, metric);
        if (sb == null || sb.minValue == null) return 0;
        return sb.minValue;
    }

    private int getMax(String section, String profile, String metric) {
        BudgetConfig config = getActiveBudgetConfig();
        SectionBudget sb = dao.loadSectionBudget(config.id, section, profile, metric);
        if (sb == null || sb.maxValue == null) return 0;
        return sb.maxValue;
    }
}

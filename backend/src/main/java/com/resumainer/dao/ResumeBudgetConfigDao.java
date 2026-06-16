package com.resumainer.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO for reading resume budget configuration from DB.
 * Reads from resume_budget_configs and resume_section_budget_rules tables.
 * All reads are owner-unaware — budget configs are global settings.
 */
@Repository
public class ResumeBudgetConfigDao {

    private static final Logger log = LoggerFactory.getLogger(ResumeBudgetConfigDao.class);

    private static final String SELECT_ACTIVE_CONFIG =
            "SELECT id, name, version_no FROM resume_budget_configs WHERE is_active = TRUE LIMIT 1";

    private static final String SELECT_SECTION_BUDGET =
            "SELECT min_value, max_value FROM resume_section_budget_rules "
            + "WHERE config_id = ? AND section_key = ? AND profile_key = ? AND metric_key = ?";

    private static final String SELECT_ALL_SECTION_BUDGETS =
            "SELECT metric_key, min_value, max_value FROM resume_section_budget_rules "
            + "WHERE config_id = ? AND section_key = ? AND profile_key = ? "
            + "ORDER BY id";

    private static final String SELECT_WORK_EXPERIENCE_DISTRIBUTION_RULES =
            "SELECT case_key, min_total_jobs, max_total_jobs, min_projects, max_projects, "
                    + "require_no_courses, template_mode, page1_jobs, page2_jobs, "
                    + "page2_max_additional_jobs, priority "
                    + "FROM resume_work_experience_distribution_rules "
                    + "WHERE config_id = ? "
                    + "ORDER BY priority ASC, id ASC";

    private final DataSource dataSource;

    public ResumeBudgetConfigDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** Minimal budget config projection. */
    public static class BudgetConfig {
        public final long id;
        public final String name;
        public final int versionNo;

        public BudgetConfig(long id, String name, int versionNo) {
            this.id = id;
            this.name = name;
            this.versionNo = versionNo;
        }
    }

    /** Single section budget metric value. */
    public static class SectionBudget {
        public final Integer minValue;
        public final Integer maxValue;

        public SectionBudget(Integer minValue, Integer maxValue) {
            this.minValue = minValue;
            this.maxValue = maxValue;
        }
    }

    /** Work experience distribution rule projection. */
    public static class WorkExperienceDistributionRule {
        public final String caseKey;
        public final int minTotalJobs;
        public final int maxTotalJobs;
        public final int minProjects;
        public final Integer maxProjects;
        public final boolean requireNoCourses;
        public final String templateMode;
        public final int page1Jobs;
        public final int page2Jobs;
        public final Integer page2MaxAdditionalJobs;
        public final int priority;

        public WorkExperienceDistributionRule(
                String caseKey,
                int minTotalJobs,
                int maxTotalJobs,
                int minProjects,
                Integer maxProjects,
                boolean requireNoCourses,
                String templateMode,
                int page1Jobs,
                int page2Jobs,
                Integer page2MaxAdditionalJobs,
                int priority
        ) {
            this.caseKey = caseKey;
            this.minTotalJobs = minTotalJobs;
            this.maxTotalJobs = maxTotalJobs;
            this.minProjects = minProjects;
            this.maxProjects = maxProjects;
            this.requireNoCourses = requireNoCourses;
            this.templateMode = templateMode;
            this.page1Jobs = page1Jobs;
            this.page2Jobs = page2Jobs;
            this.page2MaxAdditionalJobs = page2MaxAdditionalJobs;
            this.priority = priority;
        }
    }

    /**
     * Loads the single active budget config.
     * @return the active config, or null if none is active
     */
    public BudgetConfig loadActiveConfig() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ACTIVE_CONFIG);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                BudgetConfig config = new BudgetConfig(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getInt("version_no"));
                log.debug("Loaded active budget config: id={}, name={}, version={}",
                        config.id, config.name, config.versionNo);
                return config;
            }
            log.warn("No active resume budget config found");
            return null;
        } catch (SQLException e) {
            log.error("Error loading active budget config", e);
            throw new RuntimeException("Database error loading budget config", e);
        }
    }

    /**
     * Loads a single section budget metric.
     * @param configId the budget config ID
     * @param sectionKey section identifier (e.g. "skills", "courses", "projects")
     * @param profileKey profile density key (e.g. "light", "medium", "dense")
     * @return the section budget, or null if not found
     */
    public SectionBudget loadSectionBudget(long configId, String sectionKey, String profileKey, String metricKey) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_SECTION_BUDGET)) {
            stmt.setLong(1, configId);
            stmt.setString(2, sectionKey);
            stmt.setString(3, profileKey);
            stmt.setString(4, metricKey);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Integer min = rs.getObject("min_value", Integer.class);
                    Integer max = rs.getObject("max_value", Integer.class);
                    return new SectionBudget(min, max);
                }
                return null;
            }
        } catch (SQLException e) {
            log.error("Error loading section budget for {}/{}/{}", sectionKey, profileKey, metricKey, e);
            throw new RuntimeException("Database error loading section budget", e);
        }
    }

    /**
     * Loads ALL budget metrics for a section/profile combination.
     * @return Map of metricKey → SectionBudget, never null (may be empty)
     */
    public Map<String, SectionBudget> loadAllSectionBudgets(long configId, String sectionKey, String profileKey) {
        Map<String, SectionBudget> result = new LinkedHashMap<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_SECTION_BUDGETS)) {
            stmt.setLong(1, configId);
            stmt.setString(2, sectionKey);
            stmt.setString(3, profileKey);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String metricKey = rs.getString("metric_key");
                    Integer min = rs.getObject("min_value", Integer.class);
                    Integer max = rs.getObject("max_value", Integer.class);
                    result.put(metricKey, new SectionBudget(min, max));
                }
            }
        } catch (SQLException e) {
            log.error("Error loading section budgets for {}/{}", sectionKey, profileKey, e);
            throw new RuntimeException("Database error loading section budgets", e);
        }
        return result;
    }

    /**
     * Loads all Work Experience distribution rules for the selected budget config.
     * Rules are ordered by priority so that special cases are evaluated first.
     */
    public List<WorkExperienceDistributionRule> loadWorkExperienceDistributionRules(long configId) {
        List<WorkExperienceDistributionRule> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_WORK_EXPERIENCE_DISTRIBUTION_RULES)) {
            stmt.setLong(1, configId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new WorkExperienceDistributionRule(
                            rs.getString("case_key"),
                            rs.getInt("min_total_jobs"),
                            rs.getInt("max_total_jobs"),
                            rs.getInt("min_projects"),
                            rs.getObject("max_projects", Integer.class),
                            rs.getBoolean("require_no_courses"),
                            rs.getString("template_mode"),
                            rs.getInt("page1_jobs"),
                            rs.getInt("page2_jobs"),
                            rs.getObject("page2_max_additional_jobs", Integer.class),
                            rs.getInt("priority")
                    ));
                }
            }
        } catch (SQLException e) {
            log.error("Error loading work experience distribution rules for configId={}", configId, e);
            throw new RuntimeException("Database error loading work experience distribution rules", e);
        }

        return result;
    }
}

package com.resumainer.service;

import com.resumainer.dao.ResumeBudgetConfigDao;
import com.resumainer.dao.ResumeBudgetConfigDao.BudgetConfig;
import com.resumainer.dao.ResumeBudgetConfigDao.SectionBudget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for ResumeBudgetConfigService.
 */
@MockitoSettings(strictness = Strictness.LENIENT)
class ResumeBudgetConfigServiceTest {

    @Mock
    private ResumeBudgetConfigDao dao;

    private ResumeBudgetConfigService service;

    @BeforeEach
    void setUp() {
        service = new ResumeBudgetConfigService(dao);
        // Most service methods first call getActiveBudgetConfig(), which needs a stubbed DAO
        when(dao.loadActiveConfig()).thenReturn(new BudgetConfig(1L, "Test Budget", 1));
    }

    @Test
    void loadsActiveBudgetConfig() {
        BudgetConfig config = service.getActiveBudgetConfig();
        assertNotNull(config);
        assertEquals(1L, config.id);
        assertEquals("Test Budget", config.name);
        assertEquals(1, config.versionNo);
    }

    @Test
    void throwsWhenNoActiveConfig() {
        when(dao.loadActiveConfig()).thenReturn(null);
        assertThrows(IllegalStateException.class, () ->
            service.getActiveBudgetConfig());
    }

    @Test
    void loadsSkillsGroupsBudget() {
        when(dao.loadSectionBudget(1L, "skills", "light", "groups"))
                .thenReturn(new SectionBudget(4, 5));

        assertEquals(4, service.getSkillsGroups());
        assertEquals(5, service.getSkillsGroupsMax());
    }

    @Test
    void loadsSkillsPerGroupBudget() {
        when(dao.loadSectionBudget(1L, "skills", "light", "skills_per_group"))
                .thenReturn(new SectionBudget(5, 7));

        assertEquals(5, service.getSkillsPerGroup());
        assertEquals(7, service.getSkillsPerGroupMax());
    }

    @Test
    void loadsCoursesBudget() {
        when(dao.loadSectionBudget(1L, "courses", "medium", "max_courses"))
                .thenReturn(new SectionBudget(0, 7));
        when(dao.loadSectionBudget(1L, "courses", "medium", "focus_words_per_course"))
                .thenReturn(new SectionBudget(1, 3));

        assertEquals(7, service.getMaxCourses());
        assertEquals(1, service.getCourseFocusWordsMin());
        assertEquals(3, service.getCourseFocusWordsMax());
    }

    @Test
    void loadsProjectsBudget() {
        when(dao.loadSectionBudget(1L, "projects", "light", "max_projects"))
                .thenReturn(new SectionBudget(0, 4));
        when(dao.loadSectionBudget(1L, "projects", "light", "sentences_per_project"))
                .thenReturn(new SectionBudget(2, 3));
        when(dao.loadSectionBudget(1L, "projects", "light", "bullet_points_per_project"))
                .thenReturn(new SectionBudget(2, 4));

        assertEquals(4, service.getMaxProjects());
        assertEquals(2, service.getProjectSentencesMin());
        assertEquals(3, service.getProjectSentencesMax());
    }
}

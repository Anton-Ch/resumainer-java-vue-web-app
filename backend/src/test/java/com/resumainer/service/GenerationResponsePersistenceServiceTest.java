package com.resumainer.service;

import com.resumainer.dao.AiUsageLogDao;
import com.resumainer.dao.GenerationRequestDao;
import com.resumainer.dao.GenerationResponseDao;
import com.resumainer.dao.GenerationResponsePersonalDao;
import com.resumainer.model.*;
import com.resumainer.service.ai.AiClientException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Service tests for persisting parsed AI response variants.
 * Covers happy path, multiple variants, error rollback, personalInfo,
 * language/adaptation mapping, and date parsing.
 */
class GenerationResponsePersistenceServiceTest {

    private DataSource dataSource;
    private Connection connection;
    private GenerationResponseDao responseDao;
    private GenerationResponsePersonalDao personalDao;
    private GenerationRequestDao requestDao;
    private AiUsageLogDao usageLogDao;
    private GenerationResponsePersistenceService service;

    private final UUID requestId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID responseId = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        responseDao = mock(GenerationResponseDao.class);
        personalDao = mock(GenerationResponsePersonalDao.class);
        requestDao = mock(GenerationRequestDao.class);
        usageLogDao = mock(AiUsageLogDao.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(responseDao.insertResponse(any(ResumeGenerationResponse.class), same(connection)))
                .thenAnswer(invocation -> {
                    ResumeGenerationResponse response = invocation.getArgument(0);
                    response.setId(responseId);
                    return response;
                });

        service = new GenerationResponsePersistenceService(
                dataSource, responseDao, personalDao, requestDao, usageLogDao);
    }

    @Test
    void persistResponses_passesSourceIdToGeneratedSectionModels() throws Exception {
        AiResponseParser.ParsedVariant variant = makeVariant("EN", "BALANCED", true);

        service.persistResponses(requestId, userId, List.of(variant));

        ArgumentCaptor<GenerationResponseExperience> expCap = ArgumentCaptor.forClass(GenerationResponseExperience.class);
        ArgumentCaptor<GenerationResponseCourse> courseCap = ArgumentCaptor.forClass(GenerationResponseCourse.class);
        ArgumentCaptor<GenerationResponseProject> projCap = ArgumentCaptor.forClass(GenerationResponseProject.class);

        verify(responseDao).insertExperience(expCap.capture(), same(connection));
        verify(responseDao).insertCourse(courseCap.capture(), same(connection));
        verify(responseDao).insertProject(projCap.capture(), same(connection));

        assertEquals("work-5", expCap.getValue().getSourceId());
        assertEquals("course-5", courseCap.getValue().getSourceId());
        assertEquals("project-2", projCap.getValue().getSourceId());
        verify(connection).commit();
        verify(requestDao).updateStatus(requestId, userId, "completed", null, true);
    }

    @Test
    void persistResponses_insertsPersonalInfo_whenPresent() throws Exception {
        AiResponseParser.ParsedVariant variant = makeVariant("EN", "BALANCED", true);

        service.persistResponses(requestId, userId, List.of(variant));

        verify(personalDao).insert(any(GenerationResponsePersonal.class), same(connection));
    }

    @Test
    void persistResponses_skipsPersonalInfo_whenNull() throws Exception {
        AiResponseParser.ParsedVariant variant = makeVariant("EN", "BALANCED", false);

        service.persistResponses(requestId, userId, List.of(variant));

        verify(personalDao, never()).insert(any(), any());
    }

    @Test
    void persistResponses_insertsSkills() throws Exception {
        AiResponseParser.ParsedVariant variant = makeVariant("EN", "BALANCED", false);
        variant.skills.add(makeSkill("Languages", "Java"));

        service.persistResponses(requestId, userId, List.of(variant));

        verify(responseDao).insertSkill(any(GenerationResponseSkill.class), same(connection));
    }

    @Test
    void persistResponses_handlesMultipleVariants() throws Exception {
        AiResponseParser.ParsedVariant v1 = makeVariant("EN", "BALANCED", false);
        AiResponseParser.ParsedVariant v2 = makeVariant("RU", "MINIMAL", false);

        service.persistResponses(requestId, userId, List.of(v1, v2));

        verify(responseDao, times(2)).insertResponse(any(ResumeGenerationResponse.class), same(connection));
        verify(connection).commit();
    }

    @Test
    void persistResponses_mapsLanguageAndAdaptationLevels() throws Exception {
        AiResponseParser.ParsedVariant en = makeVariant("EN", "MINIMAL", false);
        AiResponseParser.ParsedVariant ru = makeVariant("RU", "MAXIMUM", false);

        service.persistResponses(requestId, userId, List.of(en, ru));

        ArgumentCaptor<ResumeGenerationResponse> captor = ArgumentCaptor.forClass(ResumeGenerationResponse.class);
        verify(responseDao, times(2)).insertResponse(captor.capture(), same(connection));

        List<ResumeGenerationResponse> responses = captor.getAllValues();
        assertEquals(1L, responses.get(0).getLanguageId());   // EN → 1
        assertEquals(1L, responses.get(0).getAdaptationLevelId()); // MINIMAL → 1
        assertEquals(2L, responses.get(1).getLanguageId());   // RU → 2
        assertEquals(3L, responses.get(1).getAdaptationLevelId()); // MAXIMUM → 3
        assertEquals(1L, responses.get(0).getStatusId());     // DRAFT
    }

    // ─── Error handling ───────────────────────────────────────────

    @Test
    void persistResponses_rollsBackAndUpdatesStatus_onSqlError() throws Exception {
        when(responseDao.insertResponse(any(ResumeGenerationResponse.class), same(connection)))
                .thenThrow(new SQLException("Insert failed"));

        AiResponseParser.ParsedVariant variant = makeVariant("EN", "BALANCED", false);

        assertThrows(IllegalStateException.class,
                () -> service.persistResponses(requestId, userId, List.of(variant)));

        verify(connection).rollback();
        verify(requestDao).updateStatus(eq(requestId), eq(userId), eq("failed"), any(), eq(false));
    }

    @Test
    void persistResponses_rethrowsAiClientException() throws Exception {
        when(responseDao.insertResponse(any(ResumeGenerationResponse.class), same(connection)))
                .thenThrow(new AiClientException("AI error"));

        AiResponseParser.ParsedVariant variant = makeVariant("EN", "BALANCED", false);

        assertThrows(AiClientException.class,
                () -> service.persistResponses(requestId, userId, List.of(variant)));

        verify(connection).rollback();
    }

    // ─── Date parsing ─────────────────────────────────────────────

    @Test
    void persistResponses_parsesDateFormats() throws Exception {
        AiResponseParser.ParsedVariant variant = makeVariant("EN", "BALANCED", true);
        variant.experience.get(0).startDate = "2025-01-15";
        variant.experience.get(0).endDate = "2025-01";
        variant.personalInfo.dateOfBirth = "1995-06-15";
        variant.personalInfo.workFormats = List.of("remote", "hybrid");

        service.persistResponses(requestId, userId, List.of(variant));

        ArgumentCaptor<GenerationResponseExperience> captor = ArgumentCaptor.forClass(GenerationResponseExperience.class);
        verify(responseDao).insertExperience(captor.capture(), same(connection));
        assertNotNull(captor.getValue().getStartDate());
        assertNotNull(captor.getValue().getEndDate());
    }

    // ─── Helpers ──────────────────────────────────────────────────

    private AiResponseParser.ParsedVariant makeVariant(String lang, String adaptation, boolean withPersonalInfo) {
        AiResponseParser.ParsedVariant v = new AiResponseParser.ParsedVariant();
        v.languageCode = lang;
        v.adaptationLevel = adaptation;
        v.professionalTitle = "Business Analyst";
        v.professionalSummary = "Summary";
        v.professionalAspirations = "Aspirations";
        v.valueLine = "Value line";
        v.coverLetter = null;

        AiResponseParser.ExperienceItem exp = new AiResponseParser.ExperienceItem();
        exp.sourceId = "work-5";
        exp.jobTitle = "Business Analyst";
        exp.companyName = "Bobrosoft";
        exp.description = "Gathered requirements.";
        exp.location = "Astana";
        exp.startDate = "2025-05";
        exp.endDate = null;
        exp.isFirstPage = true;
        v.experience.add(exp);

        AiResponseParser.CourseItem course = new AiResponseParser.CourseItem();
        course.sourceId = "course-5";
        course.name = "Microsoft Business Analysis";
        course.provider = "Coursera";
        course.courseFocus = "Business analysis";
        v.courses.add(course);

        AiResponseParser.ProjectItem proj = new AiResponseParser.ProjectItem();
        proj.sourceId = "project-2";
        proj.projectName = "Reporting Optimization";
        proj.role = "Developer";
        proj.description = "Optimized reporting workflow.";
        proj.startDate = "2026-05";
        proj.endDate = null;
        v.projects.add(proj);

        if (withPersonalInfo) {
            AiResponseParser.PersonalInfoItem pi = new AiResponseParser.PersonalInfoItem();
            pi.location = "Astana";
            pi.spokenLanguages = "English, Russian";
            pi.willingnessToRelocate = "Yes";
            pi.willingnessForBusinessTrips = "No";
            pi.citizenship = "Kazakhstan";
            pi.dateOfBirth = "1995-06-15";
            pi.workFormats = List.of("remote");
            v.personalInfo = pi;
        }

        return v;
    }

    private AiResponseParser.SkillItem makeSkill(String group, String name) {
        AiResponseParser.SkillItem s = new AiResponseParser.SkillItem();
        s.skillGroup = group;
        s.skillName = name;
        return s;
    }
}

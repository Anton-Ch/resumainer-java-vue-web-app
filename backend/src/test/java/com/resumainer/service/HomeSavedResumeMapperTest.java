package com.resumainer.service;

import com.resumainer.dto.home.HomeSavedResumeDto;
import com.resumainer.model.SavedResume;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for HomeSavedResumeMapper.
 * Verifies mapping from SavedResume entity to HomeSavedResumeDto.
 */
@ExtendWith(MockitoExtension.class)
class HomeSavedResumeMapperTest {

    @Mock
    private PublicUrlService publicUrlService;

    @Mock
    private HttpServletRequest request;

    private HomeSavedResumeMapper mapper;

    private SavedResume createTestResume() {
        SavedResume r = new SavedResume();
        r.setId(42L);
        r.setResumeTitle("Senior Engineer - Acme Corp");
        r.setVacancy("Senior Engineer");
        r.setCompany("Acme Corp");
        r.setLanguage("EN");
        r.setAdaptationLevel("BALANCED");
        r.setCreatedAt("2026-06-20");
        r.setUsername("johndoe");
        r.setPublicCode("GTFQ");
        r.setPdfStatus("READY");
        r.setPdfFilePresent(true);
        r.setHtmlFilePresent(true);
        r.setCoverLetter("Dear Hiring Manager...");
        return r;
    }

    @BeforeEach
    void setUp() {
        mapper = new HomeSavedResumeMapper(publicUrlService);
    }

    @Test
    void toDto_mapsAllFields() {
        SavedResume resume = createTestResume();
        when(publicUrlService.buildPublicUrl("johndoe", "GTFQ", request))
                .thenReturn("http://localhost:8080/johndoe/GTFQ");

        HomeSavedResumeDto dto = mapper.toDto(resume, request);

        assertEquals(42L, dto.getId());
        assertEquals("Senior Engineer - Acme Corp", dto.getResumeTitle());
        assertEquals("Senior Engineer", dto.getVacancyTitle());
        assertEquals("Acme Corp", dto.getCompanyName());
        assertEquals("EN", dto.getLanguageCode());
        assertEquals("BALANCED", dto.getAdaptationLevel());
        assertEquals("2026-06-20", dto.getCreatedAt());
    }

    @Test
    void toDto_publicUrlLink_isFullAbsoluteUrl() {
        SavedResume resume = createTestResume();
        when(publicUrlService.buildPublicUrl("johndoe", "GTFQ", request))
                .thenReturn("http://localhost:8080/johndoe/GTFQ");

        HomeSavedResumeDto dto = mapper.toDto(resume, request);

        assertEquals("http://localhost:8080/johndoe/GTFQ", dto.getPublicUrlLink());
        assertTrue(dto.getPublicUrlLink().startsWith("http"),
                "publicUrlLink must be an absolute URL");
    }

    @Test
    void toDto_canonicalExportEndpoints() {
        SavedResume resume = createTestResume();
        when(publicUrlService.buildPublicUrl("johndoe", "GTFQ", request))
                .thenReturn("http://localhost:8080/johndoe/GTFQ");

        HomeSavedResumeDto dto = mapper.toDto(resume, request);

        assertEquals("/api/generate/resumes/42/pdf?disposition=inline", dto.getPdfOpenUrl());
        assertEquals("/api/generate/resumes/42/pdf", dto.getPdfDownloadUrl());
        assertEquals("/api/generate/resumes/42/html", dto.getHtmlDownloadUrl());
    }

    @Test
    void toDto_pdfAvailable_whenReadyAndFilePresent() {
        SavedResume resume = createTestResume();
        when(publicUrlService.buildPublicUrl("johndoe", "GTFQ", request))
                .thenReturn("http://localhost:8080/johndoe/GTFQ");

        HomeSavedResumeDto dto = mapper.toDto(resume, request);

        assertTrue(dto.isPdfAvailable());
        assertEquals("READY", dto.getPdfStatus());
        assertNull(dto.getPdfMessage());
    }

    @Test
    void toDto_pdfNotAvailable_whenStatusNotReady() {
        SavedResume resume = createTestResume();
        resume.setPdfStatus("PENDING");
        resume.setPdfFilePresent(false);
        when(publicUrlService.buildPublicUrl("johndoe", "GTFQ", request))
                .thenReturn("http://localhost:8080/johndoe/GTFQ");

        HomeSavedResumeDto dto = mapper.toDto(resume, request);

        assertFalse(dto.isPdfAvailable());
        assertEquals("PENDING", dto.getPdfStatus());
        // pdfMessage is null because frontend owns i18n of UI text (FR-023)
        assertNull(dto.getPdfMessage());
    }

    @Test
    void toDto_pdfNotAvailable_whenFileNotPresentDespiteReadyStatus() {
        SavedResume resume = createTestResume();
        resume.setPdfStatus("READY");
        resume.setPdfFilePresent(false);
        when(publicUrlService.buildPublicUrl("johndoe", "GTFQ", request))
                .thenReturn("http://localhost:8080/johndoe/GTFQ");

        HomeSavedResumeDto dto = mapper.toDto(resume, request);

        assertFalse(dto.isPdfAvailable(),
                "pdfAvailable must be false when PDF file is missing even if status is READY");
    }

    @Test
    void toDto_htmlDownloadUrl_null_whenHtmlFileNotPresent() {
        SavedResume resume = createTestResume();
        resume.setHtmlFilePresent(false);
        when(publicUrlService.buildPublicUrl("johndoe", "GTFQ", request))
                .thenReturn("http://localhost:8080/johndoe/GTFQ");

        HomeSavedResumeDto dto = mapper.toDto(resume, request);

        assertNull(dto.getHtmlDownloadUrl(),
                "htmlDownloadUrl must be null when HTML file is not present");
    }

    @Test
    void toDto_coverLetter_mapsCorrectly() {
        SavedResume resume = createTestResume();
        when(publicUrlService.buildPublicUrl("johndoe", "GTFQ", request))
                .thenReturn("http://localhost:8080/johndoe/GTFQ");

        HomeSavedResumeDto dto = mapper.toDto(resume, request);

        assertEquals("Dear Hiring Manager...", dto.getCoverLetter());
    }

    @Test
    void toDto_noRawPathsExposed() {
        SavedResume resume = createTestResume();
        when(publicUrlService.buildPublicUrl("johndoe", "GTFQ", request))
                .thenReturn("http://localhost:8080/johndoe/GTFQ");

        HomeSavedResumeDto dto = mapper.toDto(resume, request);

        // Verify no old/raw fields are exposed in the DTO
        assertNull(getField(dto, "publicUrl"),
                "DTO must not expose old publicUrl field");
        assertNull(getField(dto, "pdfUrl"),
                "DTO must not expose old pdfUrl field");
    }

    @Test
    void toDto_usesRequestAwarePublicUrlBuild() {
        SavedResume resume = createTestResume();
        when(publicUrlService.buildPublicUrl("johndoe", "GTFQ", request))
                .thenReturn("https://resumainer.com/johndoe/GTFQ");

        mapper.toDto(resume, request);

        // Verify request-aware overload was called, not no-request overload
        verify(publicUrlService).buildPublicUrl("johndoe", "GTFQ", request);
        verify(publicUrlService, never()).buildPublicUrl("johndoe", "GTFQ");
    }

    /**
     * Reflection helper to check that a field does NOT exist on the DTO class.
     */
    private static Object getField(Object obj, String fieldName) {
        try {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException e) {
            return null; // Field does not exist — this is the expected result
        } catch (Exception e) {
            return null;
        }
    }
}

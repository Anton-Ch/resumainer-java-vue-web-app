package com.resumainer.service;

import com.resumainer.dto.home.HomeSavedResumeDto;
import com.resumainer.model.SavedResume;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Maps {@link SavedResume} entity data to {@link HomeSavedResumeDto}.
 * <p>
 * Uses {@link PublicUrlService} to build full absolute public URLs.
 * Requires {@link HttpServletRequest} for public URL resolution
 * (supports APP_PUBLIC_BASE_URL, forwarded headers, and request origin fallback).
 * <p>
 * Canonical PDF/HTML download URLs are constructed as relative paths
 * using the {@code /api/generate/resumes/{id}/...} contract from GenerateResumeController.
 * HTML download URL is null when the HTML file is not present.
 * <p>
 * Shared between paginated list ({@code GET /api/resumes})
 * and latest-resume summary ({@code GET /api/user/home} → summary.lastResume).
 */
@Component
public class HomeSavedResumeMapper {

    private static final String PDF_ENDPOINT = "/api/generate/resumes/";
    private static final String PDF_OPEN_SUFFIX = "/pdf?disposition=inline";
    private static final String PDF_DOWNLOAD_SUFFIX = "/pdf";
    private static final String HTML_DOWNLOAD_SUFFIX = "/html";

    private final PublicUrlService publicUrlService;

    public HomeSavedResumeMapper(PublicUrlService publicUrlService) {
        this.publicUrlService = publicUrlService;
    }

    /**
     * Maps a {@link SavedResume} to a {@link HomeSavedResumeDto}.
     *
     * @param resume  the saved resume with username and publicCode populated
     * @param request the current HTTP request (for public URL resolution)
     * @return populated HomeSavedResumeDto
     */
    public HomeSavedResumeDto toDto(SavedResume resume, HttpServletRequest request) {
        HomeSavedResumeDto dto = new HomeSavedResumeDto();

        dto.setId(resume.getId());
        dto.setResumeTitle(resume.getResumeTitle());
        dto.setVacancyTitle(resume.getVacancy());
        dto.setCompanyName(resume.getCompany());
        dto.setLanguageCode(resume.getLanguage());
        dto.setAdaptationLevel(resume.getAdaptationLevel());
        dto.setCreatedAt(resume.getCreatedAt());

        // Build full absolute public URL using request-aware resolution
        if (resume.getUsername() != null && resume.getPublicCode() != null) {
            String publicUrlLink = publicUrlService.buildPublicUrl(
                    resume.getUsername(), resume.getPublicCode(), request);
            dto.setPublicUrlLink(publicUrlLink);
        }

        // Canonical authenticated export endpoints (relative paths)
        long id = resume.getId();
        dto.setPdfOpenUrl(PDF_ENDPOINT + id + PDF_OPEN_SUFFIX);
        dto.setPdfDownloadUrl(PDF_ENDPOINT + id + PDF_DOWNLOAD_SUFFIX);
        // HTML download URL only if HTML file is present (checked via pdfStatus or field presence)
        dto.setHtmlDownloadUrl(PDF_ENDPOINT + id + HTML_DOWNLOAD_SUFFIX);

        // PDF availability: status READY AND PDF file physically present
        boolean pdfReady = "READY".equals(resume.getPdfStatus()) && resume.isPdfFilePresent();
        dto.setPdfAvailable(pdfReady);
        dto.setPdfStatus(resume.getPdfStatus());
        dto.setPdfMessage(pdfReady ? null : "PDF is being generated. Please try again later.");

        // HTML download URL: only set when HTML file is present
        if (!resume.isHtmlFilePresent()) {
            dto.setHtmlDownloadUrl(null);
        }

        dto.setCoverLetter(resume.getCoverLetter());

        return dto;
    }
}

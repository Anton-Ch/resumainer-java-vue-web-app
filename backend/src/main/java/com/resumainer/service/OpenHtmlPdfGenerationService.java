package com.resumainer.service;

import com.resumainer.model.PdfFitLimits;
import com.resumainer.model.pdf.*;
import com.resumainer.service.pdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * Real PDF/HTML generation service for Feature 008.
 * Replaces the NoOp stub with the full rendering pipeline:
 * XHTML template → OpenHTMLToPDF → PdfAnalyzer → PdfValidationService → FeedbackFitEngine.
 *
 * Also implements the legacy PdfGenerationService interface for backward compatibility.
 */
@Service
public class OpenHtmlPdfGenerationService implements PdfGenerationService {

    private static final Logger log = LoggerFactory.getLogger(OpenHtmlPdfGenerationService.class);

    private final XhtmlTemplateRenderer templateRenderer;
    private final OpenHtmlPdfRenderer pdfRenderer;
    private final PdfAnalyzer analyzer;
    private final PdfValidationService validator;
    private final PdfRenderConfigService configService;
    private volatile FeedbackFitEngine fitEngine; // lazy-init after Flyway migrations

    public OpenHtmlPdfGenerationService(PdfRenderConfigService configService) {
        this.configService = configService;
        this.templateRenderer = new XhtmlTemplateRenderer();
        String fontsPath = OpenHtmlPdfGenerationService.class.getClassLoader()
                .getResource("fonts") != null
                ? OpenHtmlPdfGenerationService.class.getClassLoader().getResource("fonts").getPath()
                : ".";
        this.pdfRenderer = new OpenHtmlPdfRenderer(new File(fontsPath));
        this.analyzer = new PdfAnalyzer();
        this.validator = new PdfValidationService();
    }

    private FeedbackFitEngine getFitEngine() {
        if (fitEngine == null) {
            synchronized (this) {
                if (fitEngine == null) {
                    PdfFitLimits limits = configService.getActiveFitLimits();
                    fitEngine = new FeedbackFitEngine(templateRenderer, pdfRenderer, analyzer, validator, limits);
                }
            }
        }
        return fitEngine;
    }

    /**
     * Generate PDF + parity HTML for a resume using the full rendering pipeline.
     *
     * @param renderData the assembled resume render data
     * @param pagePlan   the page allocation plan from budget resolver
     * @param outputDir  directory to write HTML and PDF files (staging or final)
     * @param baseName   base filename without extension (e.g., "resume_en")
     * @return generation result with paths to generated files and metrics
     */
    public PdfGenerationResult generate(ResumeRenderData renderData, PagePlan pagePlan,
                                         File outputDir, String baseName) {
        File htmlFile = new File(outputDir, baseName + ".html");
        File pdfFile = new File(outputDir, baseName + ".pdf");
        File debugDir = new File(outputDir, "debug");

        FitResult result = getFitEngine().fit(renderData, pagePlan,
                null, // targets loaded from config by fit engine
                htmlFile, pdfFile, debugDir, false);

        FitAttempt selected = result.selectedAttempt();
        if (selected == null || !selected.valid()) {
            String reason = selected != null ? selected.reason() : "no valid attempt";
            log.warn("PDF fitting failed: {}", reason);
            return PdfGenerationResult.failure(reason);
        }

        log.info("PDF generated: {} pages, fill={}",
                selected.metrics().actualPageCount(), selected.metrics().fillRatios());
        return PdfGenerationResult.success(
                htmlFile.getAbsolutePath(),
                pdfFile.getAbsolutePath(),
                selected.metrics().actualPageCount(),
                selected.state());
    }

    // Legacy interface (not used by new finalization flow, kept for contract compliance)
    @Override
    public boolean convertPdf(String htmlFilePath, String pdfFilePath) {
        log.warn("convertPdf() called on OpenHtmlPdfGenerationService — use generate() instead");
        return false;
    }

    /** Result of a PDF generation run. */
    public static class PdfGenerationResult {
        private final boolean success;
        private final String htmlPath;
        private final String pdfPath;
        private final int pageCount;
        private final FitState fitState;
        private final String errorReason;

        private PdfGenerationResult(boolean success, String htmlPath, String pdfPath,
                                     int pageCount, FitState fitState, String errorReason) {
            this.success = success;
            this.htmlPath = htmlPath;
            this.pdfPath = pdfPath;
            this.pageCount = pageCount;
            this.fitState = fitState;
            this.errorReason = errorReason;
        }

        public static PdfGenerationResult success(String htmlPath, String pdfPath, int pageCount, FitState fitState) {
            return new PdfGenerationResult(true, htmlPath, pdfPath, pageCount, fitState, null);
        }

        public static PdfGenerationResult failure(String reason) {
            return new PdfGenerationResult(false, null, null, 0, null, reason);
        }

        public boolean isSuccess() { return success; }
        public String getHtmlPath() { return htmlPath; }
        public String getPdfPath() { return pdfPath; }
        public int getPageCount() { return pageCount; }
        public FitState getFitState() { return fitState; }
        public String getErrorReason() { return errorReason; }
    }
}

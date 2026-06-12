package com.resumainer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Placeholder PDF service for feat/007.
 * Reports that PDF generation is not available in this feature.
 * Real PDF conversion is implemented in feat/008-pdf-conversion.
 */
@Service
public class NoOpPdfGenerationService implements PdfGenerationService {

    private static final Logger log = LoggerFactory.getLogger(NoOpPdfGenerationService.class);

    @Override
    public boolean convertPdf(String htmlFilePath, String pdfFilePath) {
        log.warn("PDF conversion is not available in feat/007. "
                + "HTML saved at: {}. PDF would be at: {}",
                htmlFilePath, pdfFilePath);
        return false;
    }
}

package com.resumainer.service;

/**
 * Boundary interface for server-side HTML-to-PDF conversion.
 * In feat/007, only the interface and a NoOp stub exist.
 * Real PDF conversion is implemented in feat/008-pdf-conversion.
 */
public interface PdfGenerationService {

    /**
     * Converts a saved HTML file to PDF.
     *
     * @param htmlFilePath  the absolute path to the saved HTML file
     * @param pdfFilePath   the desired path for the PDF output
     * @return true if conversion succeeded
     */
    boolean convertPdf(String htmlFilePath, String pdfFilePath);
}

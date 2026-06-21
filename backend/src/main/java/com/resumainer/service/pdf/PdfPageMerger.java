package com.resumainer.service.pdf;

import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Merges individual page PDFs into a single multi-page PDF.
 * Ported from spike V12.1. Adapted: Path → File.
 */
public final class PdfPageMerger {

    public void merge(List<File> pagePdfs, File outputPdf) {
        try {
            Files.createDirectories(outputPdf.getParentFile().toPath());
            PDFMergerUtility merger = new PDFMergerUtility();
            merger.setDestinationFileName(outputPdf.getAbsolutePath());
            for (File pdf : pagePdfs) merger.addSource(pdf);
            merger.mergeDocuments(null);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to merge page PDFs into " + outputPdf, e);
        }
    }
}

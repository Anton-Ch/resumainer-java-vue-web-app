package com.resumainer.pdfspike.pdf;

import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class PdfPageMerger {
    public void merge(List<Path> pagePdfs, Path outputPdf) {
        try {
            Files.createDirectories(outputPdf.getParent());
            PDFMergerUtility merger = new PDFMergerUtility();
            merger.setDestinationFileName(outputPdf.toString());
            for (Path pdf : pagePdfs) merger.addSource(pdf.toFile());
            merger.mergeDocuments(null);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to merge page PDFs into " + outputPdf, e);
        }
    }
}

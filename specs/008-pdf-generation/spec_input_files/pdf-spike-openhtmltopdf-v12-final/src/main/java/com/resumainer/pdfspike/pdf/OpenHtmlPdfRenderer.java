package com.resumainer.pdfspike.pdf;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class OpenHtmlPdfRenderer {
    private final Path resourcesDir;

    public OpenHtmlPdfRenderer(Path resourcesDir) { this.resourcesDir = resourcesDir; }

    public void render(String html, Path pdfPath) {
        try {
            Files.createDirectories(pdfPath.getParent());
            try (OutputStream os = Files.newOutputStream(pdfPath)) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(html, resourcesDir.toUri().toString());
                builder.toStream(os);
                builder.run();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to render PDF: " + pdfPath, e);
        }
    }
}

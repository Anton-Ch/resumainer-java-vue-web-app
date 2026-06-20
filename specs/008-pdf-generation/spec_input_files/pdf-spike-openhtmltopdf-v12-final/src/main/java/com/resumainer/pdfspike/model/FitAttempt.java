package com.resumainer.pdfspike.model;

import java.nio.file.Path;

public record FitAttempt(
        int attemptNumber,
        int targetPageCount,
        FitState state,
        PdfMetrics metrics,
        boolean valid,
        String reason,
        Path htmlPath,
        Path pdfPath
) {}

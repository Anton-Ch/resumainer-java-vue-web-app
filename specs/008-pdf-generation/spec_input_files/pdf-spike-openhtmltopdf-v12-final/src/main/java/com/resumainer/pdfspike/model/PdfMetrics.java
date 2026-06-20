package com.resumainer.pdfspike.model;

import java.util.Map;

public record PdfMetrics(int actualPageCount, boolean selectableText, Map<Integer, Double> fillRatios, String extractedText) {}

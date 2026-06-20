package com.resumainer.pdfspike.model;

public record FillTarget(int pageCount, int pageNumber, double minFillRatio, Double maxFillRatio, boolean requiredNonEmpty) {}

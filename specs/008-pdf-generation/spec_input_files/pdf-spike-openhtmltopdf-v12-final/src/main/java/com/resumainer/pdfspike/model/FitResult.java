package com.resumainer.pdfspike.model;

import java.util.List;

public record FitResult(FitAttempt selectedAttempt, List<FitAttempt> attempts) {}

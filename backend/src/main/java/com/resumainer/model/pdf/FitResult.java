package com.resumainer.model.pdf;

import java.util.List;

/**
 * Final result of the feedback fitting process.
 * Contains the selected best attempt and all recorded attempts.
 * Ported from spike V12.1 FitResult record.
 */
public record FitResult(FitAttempt selectedAttempt, List<FitAttempt> attempts) {}

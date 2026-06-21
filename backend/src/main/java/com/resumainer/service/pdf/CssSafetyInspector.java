package com.resumainer.service.pdf;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Rejects browser-only CSS tokens unsafe for OpenHTMLToPDF.
 * Ported from spike V12.1.
 */
public final class CssSafetyInspector {
    private static final List<ForbiddenCssPattern> FORBIDDEN = List.of(
            new ForbiddenCssPattern("display:flex", Pattern.compile("display\\s*:\\s*flex", Pattern.CASE_INSENSITIVE)),
            new ForbiddenCssPattern("flex-direction", Pattern.compile("flex-direction\\s*:", Pattern.CASE_INSENSITIVE)),
            new ForbiddenCssPattern("row-gap", Pattern.compile("row-gap\\s*:", Pattern.CASE_INSENSITIVE)),
            new ForbiddenCssPattern("break-inside", Pattern.compile("(?<!page-)break-inside\\s*:", Pattern.CASE_INSENSITIVE)),
            new ForbiddenCssPattern("overflow:hidden", Pattern.compile("overflow\\s*:\\s*hidden", Pattern.CASE_INSENSITIVE))
    );

    public String inspect(String html) {
        String value = html == null ? "" : html.toLowerCase(Locale.ROOT);
        return FORBIDDEN.stream()
                .filter(pattern -> pattern.pattern().matcher(value).find())
                .findFirst()
                .map(pattern -> "FORBIDDEN_CSS_TOKEN " + pattern.label())
                .orElse("OK");
    }

    private record ForbiddenCssPattern(String label, Pattern pattern) {}
}

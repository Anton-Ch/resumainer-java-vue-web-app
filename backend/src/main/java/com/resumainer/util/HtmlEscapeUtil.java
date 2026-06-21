package com.resumainer.util;

/**
 * HTML-escape user-controlled text before insertion into XHTML/PDF templates.
 * Prevents markup injection in the HTML-to-PDF rendering pipeline (FR-008-023-1).
 */
public final class HtmlEscapeUtil {

    private HtmlEscapeUtil() {}

    /**
     * Escape HTML special characters.
     * @param s the input string, may be null
     * @return escaped string, or empty string if input is null
     */
    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

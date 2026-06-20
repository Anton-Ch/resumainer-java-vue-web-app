package com.resumainer.pdfspike.util;

public final class Escape {
    private Escape() {}
    public static String html(String v) {
        if (v == null || v.isBlank()) return "";
        return v.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}

package com.resumainer.pdfspike.model;

public enum TemplateMode {
    ONE_PAGE, TWO_PAGE;

    public static TemplateMode fromDb(String value) {
        return switch (value.toLowerCase()) {
            case "one_page" -> ONE_PAGE;
            case "two_page" -> TWO_PAGE;
            default -> throw new IllegalArgumentException("Unknown template mode: " + value);
        };
    }
}

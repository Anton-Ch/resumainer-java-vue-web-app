package com.resumainer.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HtmlEscapeUtilTest {

    @Test
    void escape_plainText_unchanged() {
        assertEquals("Hello World", HtmlEscapeUtil.escape("Hello World"));
    }

    @Test
    void escape_ampersand() {
        assertEquals("a &amp; b", HtmlEscapeUtil.escape("a & b"));
    }

    @Test
    void escape_lessThan() {
        assertEquals("&lt;div&gt;", HtmlEscapeUtil.escape("<div>"));
    }

    @Test
    void escape_greaterThan() {
        assertEquals("a &gt; b", HtmlEscapeUtil.escape("a > b"));
    }

    @Test
    void escape_doubleQuote() {
        assertEquals("&quot;hello&quot;", HtmlEscapeUtil.escape("\"hello\""));
    }

    @Test
    void escape_singleQuote() {
        assertEquals("it&#39;s", HtmlEscapeUtil.escape("it's"));
    }

    @Test
    void escape_scriptTag_full() {
        String input = "<script>alert(1)</script>";
        String escaped = HtmlEscapeUtil.escape(input);
        assertFalse(escaped.contains("<script>"), "Script tag must be escaped");
        assertTrue(escaped.contains("&lt;"), "Must contain escaped less-than");
    }

    @Test
    void escape_null_returnsEmpty() {
        assertEquals("", HtmlEscapeUtil.escape(null));
    }

    @Test
    void escape_empty_returnsEmpty() {
        assertEquals("", HtmlEscapeUtil.escape(""));
    }

    @Test
    void escape_mixedTextAndTags() {
        String input = "<b>hello</b> & welcome";
        String escaped = HtmlEscapeUtil.escape(input);
        assertEquals("&lt;b&gt;hello&lt;/b&gt; &amp; welcome", escaped);
    }
}

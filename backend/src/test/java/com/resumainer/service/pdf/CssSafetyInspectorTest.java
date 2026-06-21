package com.resumainer.service.pdf;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CssSafetyInspectorTest {
    @Test void rejectsBrowserOnlyCss() {
        assertTrue(new CssSafetyInspector().inspect(".x{display:flex;row-gap:10px;}").startsWith("FORBIDDEN_CSS_TOKEN"));
    }

    @Test void rejectsModernBreakInsideButAllowsPageBreakInside() {
        assertTrue(new CssSafetyInspector().inspect(".x{break-inside:avoid;}").startsWith("FORBIDDEN_CSS_TOKEN"));
        assertEquals("OK", new CssSafetyInspector().inspect(".x{page-break-inside:avoid;}"));
    }

    @Test void acceptsPdfSafeCss() {
        assertEquals("OK", new CssSafetyInspector().inspect("section{margin-bottom:12px;} .x{page-break-inside:avoid;}"));
    }
}

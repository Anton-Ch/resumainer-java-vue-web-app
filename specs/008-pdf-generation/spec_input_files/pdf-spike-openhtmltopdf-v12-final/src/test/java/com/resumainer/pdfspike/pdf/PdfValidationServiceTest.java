package com.resumainer.pdfspike.pdf;

import com.resumainer.pdfspike.model.*;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class PdfValidationServiceTest {
    @Test void acceptsPageWithinTargets() {
        PdfMetrics m = new PdfMetrics(2,true,Map.of(1,0.90,2,0.60),"text");
        Map<Integer, FillTarget> targets = Map.of(1,new FillTarget(2,1,0.85,0.96,true),2,new FillTarget(2,2,0.50,0.96,true));
        assertEquals("OK", new PdfValidationService().validate(m,2,targets,java.util.List.of("text")));
    }

    @Test void rejectsUnderfilledPage() {
        PdfMetrics m = new PdfMetrics(2,true,Map.of(1,0.90,2,0.30),"text");
        Map<Integer, FillTarget> targets = Map.of(1,new FillTarget(2,1,0.85,0.96,true),2,new FillTarget(2,2,0.50,0.96,true));
        assertTrue(new PdfValidationService().validate(m,2,targets,java.util.List.of("text")).startsWith("UNDERFILLED"));
    }

    @Test void normalizesLetterHyphenDifferencesFromPdfExtraction() {
        PdfMetrics m = new PdfMetrics(1,true,Map.of(1,0.90),"Мне интересны системы где пересекаются бизнесправила и данные");
        Map<Integer, FillTarget> targets = Map.of(1,new FillTarget(1,1,0.80,0.96,true));
        assertEquals("OK", new PdfValidationService().validate(m,1,targets,java.util.List.of("пересекаются бизнес-правила и данные")));
    }
}

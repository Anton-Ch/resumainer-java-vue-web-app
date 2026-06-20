package com.resumainer.pdfspike.budget;

import com.resumainer.pdfspike.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BudgetResolverTest {
    @Test void resolvesKnownRuleAndValidatesCounts() {
        EdgeCaseRuleProvider provider = ec -> new EdgeCaseRule(4,1,1,1,1, TemplateMode.TWO_PAGE,1,0,1,"project driven");
        BudgetResolver resolver = new BudgetResolver(provider);
        EdgeCaseRule rule = resolver.resolve(4,1,1,1);
        assertEquals(TemplateMode.TWO_PAGE, rule.templateMode());
        assertEquals(2, rule.expectedPages());
    }

    @Test void rejectsWrongCounts() {
        EdgeCaseRuleProvider provider = ec -> new EdgeCaseRule(1,1,1,0,1, TemplateMode.ONE_PAGE,1,0,1,"one job");
        assertThrows(IllegalStateException.class, () -> new BudgetResolver(provider).resolve(1,2,0,1));
    }
}

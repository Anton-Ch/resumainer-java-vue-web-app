package com.resumainer.pdfspike.budget;

import com.resumainer.pdfspike.model.EdgeCaseRule;

/**
 * Production concept, spike data source.
 * Keep the resolver idea, but replace EdgeCaseRuleProvider with the real capstone budget/config DAO/service.
 */
public final class BudgetResolver {
    private final EdgeCaseRuleProvider ruleProvider;
    public BudgetResolver(EdgeCaseRuleProvider ruleProvider) { this.ruleProvider = ruleProvider; }

    public EdgeCaseRule resolve(int ecNumber, int workCount, int projectCount, int courseCount) {
        EdgeCaseRule rule = ruleProvider.loadRule(ecNumber);
        if (workCount < rule.minWork() || workCount > rule.maxWork()) {
            throw new IllegalStateException("Fixture work count does not match EC-" + ecNumber + ": " + workCount);
        }
        if (projectCount != rule.projectCount()) {
            throw new IllegalStateException("Fixture project count does not match EC-" + ecNumber + ": " + projectCount);
        }
        if (courseCount != rule.courseCount()) {
            throw new IllegalStateException("Fixture course count does not match EC-" + ecNumber + ": " + courseCount);
        }
        return rule;
    }
}

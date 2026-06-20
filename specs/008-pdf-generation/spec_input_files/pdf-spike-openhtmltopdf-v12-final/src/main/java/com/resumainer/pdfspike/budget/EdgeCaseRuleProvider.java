package com.resumainer.pdfspike.budget;

import com.resumainer.pdfspike.model.EdgeCaseRule;

@FunctionalInterface
/**
 * SPIKE ONLY adapter for edge-case rule lookup.
 * The production app should use real budget/render configuration instead of edge_case_rule.
 */
public interface EdgeCaseRuleProvider {
    EdgeCaseRule loadRule(int ecNumber);
}

package com.resumainer.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock AI client for development and automated tests.
 * Returns deterministic sample JSON matching the expected response contract.
 * Never calls a real external API.
 */
public class MockAiClient implements AiClient {

    private static final Logger log = LoggerFactory.getLogger(MockAiClient.class);

    /** Returns a sample JSON response that matches the expected contract. */
    @Override
    public String generate(String systemPrompt, String requestPrompt) throws AiClientException {
        log.debug("MockAiClient.generate() called — returning sample response");
        return SAMPLE_RESPONSE;
    }

    private static final String SAMPLE_RESPONSE = """
            {
              "professionalTitle": "Senior Java Developer",
              "valueLine": "Experienced Java developer with Spring expertise",
              "professionalSummary": "Backend developer with 5+ years of experience in Java, Spring, and PostgreSQL.",
              "professionalAspirations": "Seeking a challenging role in a product-driven company.",
              "workExperience": [
                {
                  "jobTitle": "Java Developer",
                  "companyName": "Tech Corp",
                  "description": "Developed REST APIs using Spring MVC and JDBC.",
                  "bulletPoints": [
                    "Led backend migration from legacy monolith to Spring MVC microservices, reducing deployment time by 40%",
                    "Designed and implemented REST APIs serving 50K daily active users",
                    "Mentored 3 junior developers on Spring best practices and code review process"
                  ],
                  "location": "Astana, Kazakhstan",
                  "startDate": "2022-01",
                  "endDate": "2024-12",
                  "isFirstPage": true
                }
              ],
              "courses": [],
              "projects": [
                {
                  "projectName": "ResumAIner",
                  "role": "Backend Developer",
                  "description": "AI-assisted resume adaptation platform.",
                  "bulletPoints": [
                    "Built PDF generation pipeline using OpenHTMLToPDF and PDFBox",
                    "Implemented TDD workflow with 80%+ test coverage"
                  ],
                  "startDate": "2025-01"
                }
              ],
              "skills": [
                { "skillGroup": "Programming Languages", "skillName": "Java" },
                { "skillGroup": "Frameworks", "skillName": "Spring MVC" }
              ],
              "personalInfo": {
                "location": "Astana, Kazakhstan",
                "spokenLanguages": "English C1, Russian Native",
                "willingnessToRelocate": "Yes",
                "willingnessForBusinessTrips": "Yes",
                "citizenship": "Kazakhstan",
                "dateOfBirth": "1995-06-15",
                "workFormats": ["remote", "hybrid"]
              }
            }
            """;
}

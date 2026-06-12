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
                  "location": "Astana, Kazakhstan",
                  "startDate": "2022-01",
                  "endDate": "2024-12",
                  "isFirstPage": true
                }
              ],
              "courses": [],
              "projects": [],
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

package com.resumainer.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the ResumAIner Landing Page at the application root URL.
 * <p>
 * Maps {@code GET /} to the {@code landing} Thymeleaf view and provides
 * the externalized CTA URL for authentication flow navigation.
 */
@Controller
public class LandingPageController {

    @Value("${landing.cta.url:/auth/login}")
    private String ctaUrl;

    /**
     * Displays the Landing Page with all 8 sections (Header, Hero, Problem,
     * How It Works, Features, Trust & Control, FAQ, Final CTA).
     *
     * @param model the Spring MVC model
     * @return the "landing" view name resolved by ThymeleafViewResolver
     */
    @GetMapping("/")
    public String landing(Model model) {
        model.addAttribute("ctaUrl", ctaUrl);
        return "landing";
    }
}

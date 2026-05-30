package com.resumainer.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Home controller that serves the Hello World landing page.
 * <p>
 * Maps {@code GET /} to the {@code hello} view and populates the model
 * with the application name, current server time, and active Spring profile.
 */
@Controller
public class HelloWorldController {

    @Value("${spring.application.name:ResumAIner}")
    private String appName;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("appName", appName);
        model.addAttribute("serverTime", LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        model.addAttribute("activeProfile", System.getProperty("spring.profiles.active", "default"));
        return "hello";
    }
}

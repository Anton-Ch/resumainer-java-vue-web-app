package com.resumainer.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

/**
 * Tests for {@link LandingPageController}.
 * <p>
 * Uses standalone MockMvc setup — no Spring context needed.
 * The LandingPageController has no DB dependencies.
 */
class LandingPageControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LandingPageController controller = new LandingPageController("/auth/login");
        mockMvc = standaloneSetup(controller).build();
    }

    @Test
    void getLandingPage_returns200() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("landing"));
    }

    @Test
    void getLandingPage_containsCtaUrlAttribute() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("ctaUrl"));
    }

    @Test
    void getLandingPage_usesDefaultCtaUrl_whenNotConfigured() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("ctaUrl", "/auth/login"));
    }
}

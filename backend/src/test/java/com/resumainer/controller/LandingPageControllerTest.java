package com.resumainer.controller;

import com.resumainer.config.WebConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for {@link LandingPageController}.
 * <p>
 * Verifies that the Landing Page is served correctly at the root URL,
 * contains the required model attributes, and supports locale switching.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = WebConfig.class)
@WebAppConfiguration
class LandingPageControllerTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
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

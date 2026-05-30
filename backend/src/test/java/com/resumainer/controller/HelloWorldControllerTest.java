package com.resumainer.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit test for {@link HelloWorldController}.
 * <p>
 * Uses standalone MockMvc setup — no full Spring context required.
 * {@code @Value} fields are injected via {@link ReflectionTestUtils}.
 */
class HelloWorldControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        HelloWorldController controller = new HelloWorldController();
        ReflectionTestUtils.setField(controller, "appName", "ResumAIner");
        ReflectionTestUtils.setField(controller, "activeProfile", "dev");
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void homePage_returns200WithHelloViewAndModelAttributes() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("hello"))
                .andExpect(model().attribute("appName", "ResumAIner"))
                .andExpect(model().attributeExists("serverTime"))
                .andExpect(model().attribute("activeProfile", "dev"));
    }
}

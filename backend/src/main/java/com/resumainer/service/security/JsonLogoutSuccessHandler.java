package com.resumainer.service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.IOException;
import java.util.Map;

/**
 * Spring Security {@link LogoutSuccessHandler} that returns a JSON
 * response compatible with the existing frontend contract.
 *
 * <p>Response shape:
 * <pre>
 * { "success": true, "redirectUrl": "/auth" }
 * </pre>
 */
public class JsonLogoutSuccessHandler implements LogoutSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(JsonLogoutSuccessHandler.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException {
        if (authentication != null) {
            log.info("Logout: email={}", authentication.getName());
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");

        objectMapper.writeValue(response.getWriter(), Map.of(
                "success", true,
                "redirectUrl", "/auth"
        ));
    }
}

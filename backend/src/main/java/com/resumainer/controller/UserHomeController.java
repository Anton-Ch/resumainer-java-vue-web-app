package com.resumainer.controller;

import com.resumainer.dto.UserSession;
import com.resumainer.model.UserHomeSummary;
import com.resumainer.service.UserHomeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for the User Home summary endpoint.
 * <p>
 * Returns profile readiness, checklist state, resume counts,
 * and last resume preview for the authenticated user.
 */
@RestController
@RequestMapping("/api/user")
public class UserHomeController {

    private static final Logger log = LoggerFactory.getLogger(UserHomeController.class);

    private final UserHomeService userHomeService;

    public UserHomeController(UserHomeService userHomeService) {
        this.userHomeService = userHomeService;
    }

    /**
     * Get the home summary for the authenticated user.
     *
     * @param userSession the authenticated user session
     * @return 200 with UserHomeSummary, or 401 if not authenticated
     */
    @GetMapping("/home")
    public ResponseEntity<UserHomeSummary> getHomeSummary(
            @SessionAttribute(value = "user", required = false) UserSession userSession) {

        if (userSession == null) {
            log.warn("getHomeSummary called without valid session");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.debug("getHomeSummary: userId={}", userSession.getUserId());

        try {
            UserHomeSummary summary = userHomeService.getHomeSummary(userSession.getUserId());
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error getting home summary for user {}: {}", userSession.getUserId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

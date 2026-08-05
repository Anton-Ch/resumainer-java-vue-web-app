package com.resumainer.controller;

import com.resumainer.dto.AuthResponse;
import com.resumainer.dto.RegisterRequest;
import com.resumainer.dto.ResendVerificationRequest;
import com.resumainer.dto.ResendVerificationResponse;
import com.resumainer.model.User;
import com.resumainer.service.AuthService;
import com.resumainer.service.ResendVerificationService;
import com.resumainer.service.VerificationService;
import com.resumainer.service.security.CustomUserDetails;
import com.resumainer.service.security.TrustedProxyClientIpExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

/**
 * REST controller for authentication endpoints.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final VerificationService verificationService;
    private final ResendVerificationService resendVerificationService;
    private final TrustedProxyClientIpExtractor clientIpExtractor;
    private final String frontendBaseUrl;

    @Autowired
    public AuthController(AuthService authService,
                          VerificationService verificationService,
                          ResendVerificationService resendVerificationService,
                          TrustedProxyClientIpExtractor clientIpExtractor,
                          @Value("${app.frontend.public.base-url}") String frontendBaseUrl) {
        this.authService = authService;
        this.verificationService = verificationService;
        this.resendVerificationService = resendVerificationService;
        this.clientIpExtractor = clientIpExtractor;
        this.frontendBaseUrl = frontendBaseUrl != null ? frontendBaseUrl.replaceAll("/+$", "") : "";
    }

    /** Test/backward-compatible constructor for endpoints that do not use resend verification. */
    public AuthController(AuthService authService,
                          VerificationService verificationService,
                          String frontendBaseUrl) {
        this(authService, verificationService, null, null, frontendBaseUrl);
    }

    /**
     * Register a new user with strict email verification.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for email: {}", request.getEmail());

        try {
            User user = authService.register(request);

            if (user == null || user.getId() == null) {
                log.error("Registration returned null user for email: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(AuthResponse.failure("REGISTRATION_FAILED", "Registration failed. Please try again."));
            }

            log.info("User registered successfully (pending verification): {}", user.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(AuthResponse.pendingVerification());

        } catch (com.resumainer.exception.ServiceException e) {
            String publicCode;
            String publicMessage;
            HttpStatus status;

            switch (e.getErrorCode()) {
                case "auth.email.alreadyRegistered":
                case "auth.email.duplicate":
                    publicCode = "DUPLICATE_EMAIL";
                    publicMessage = "An account with this email already exists.";
                    status = HttpStatus.CONFLICT;
                    break;
                case "auth.password.weak":
                    publicCode = "INVALID_INPUT";
                    publicMessage = "Password does not meet strength requirements.";
                    status = HttpStatus.BAD_REQUEST;
                    break;
                case "auth.password.mismatch":
                    publicCode = "INVALID_INPUT";
                    publicMessage = "Passwords do not match.";
                    status = HttpStatus.BAD_REQUEST;
                    break;
                case "auth.captcha.invalid":
                    publicCode = "CAPTCHA_INVALID";
                    publicMessage = "CAPTCHA verification failed. Please try again.";
                    status = HttpStatus.BAD_REQUEST;
                    break;
                default:
                    publicCode = "REGISTRATION_FAILED";
                    publicMessage = "Registration failed. Please try again.";
                    status = HttpStatus.INTERNAL_SERVER_ERROR;
                    break;
            }

            log.warn("Registration failed for {}: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(status)
                    .body(AuthResponse.failure(publicCode, publicMessage));

        } catch (Exception e) {
            log.error("Unexpected registration error for {}: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.failure("REGISTRATION_FAILED", "Registration failed. Please try again."));
        }
    }

    /**
     * Handle @Valid validation failures for registration with AuthResponse format.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AuthResponse> handleValidationError(MethodArgumentNotValidException ex) {
        log.warn("Registration validation failed: errorCount={}",
                ex.getBindingResult().getErrorCount());
        return ResponseEntity.badRequest()
                .body(AuthResponse.failure("INVALID_INPUT", "Please check your input and try again."));
    }

    /**
     * Handle malformed JSON or empty body for the register endpoint.
     * Returns the same AuthResponse format as other validation failures.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<AuthResponse> handleMessageNotReadable() {
        log.warn("Registration request not readable: malformed JSON or empty body");
        return ResponseEntity.badRequest()
                .body(AuthResponse.failure("INVALID_INPUT", "Please check your input and try again."));
    }

    /**
     * Verify email via token from email link.
     */
    @GetMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestParam(value = "token", required = false) String token) {
        long start = System.nanoTime();

        VerificationService.VerifyResult result = verificationService.verify(token);

        long elapsed = System.nanoTime() - start;
        long delayMs = Math.max(0, 200 - (elapsed / 1_000_000));
        if (delayMs > 0) {
            try { Thread.sleep(delayMs); } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return switch (result) {
            case SUCCESS -> redirectToVerified("success");
            case TOKEN_EXPIRED -> redirectToVerified("expired");
            case TOKEN_INVALID -> redirectToVerified("invalid");
        };
    }

    /** Resends verification email without disclosing account existence. */
    @PostMapping("/email-verification/resend")
    public ResponseEntity<?> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request,
            HttpServletRequest servletRequest) {
        String clientIp = clientIpExtractor.extract(servletRequest);
        ResendVerificationService.Result result = resendVerificationService.resend(
                request.getEmail().trim().toLowerCase(java.util.Locale.ROOT),
                request.getCaptchaToken(), clientIp);

        return switch (result.status()) {
            case SUCCESS -> ResponseEntity.ok(ResendVerificationResponse.genericSuccess());
            case CAPTCHA_INVALID -> ResponseEntity.badRequest().body(
                    AuthResponse.failure("CAPTCHA_INVALID",
                            "CAPTCHA verification failed. Please try again."));
            case RATE_LIMITED -> ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header(HttpHeaders.RETRY_AFTER, String.valueOf(result.retryAfterSeconds()))
                    .body(AuthResponse.failure("RATE_LIMITED",
                            "Too many verification email requests. Please try again later."));
        };
    }

    /** Redirect to frontend verification result page. */
    private ResponseEntity<Void> redirectToVerified(String status) {
        String url = frontendBaseUrl + "/app/auth/verified?status=" + status;
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(url));
        return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
    }

    /** Check current authentication status from Spring Security. */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.ok(Map.of(
                    "authenticated", false, "email", "", "role", ""
            ));
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            String role = userDetails.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())) ? "ADMIN" : "USER";
            return ResponseEntity.ok(Map.of(
                    "authenticated", true,
                    "email", userDetails.getUsername(),
                    "role", role
            ));
        }

        return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "email", authentication.getName(),
                "role", ""
        ));
    }
}

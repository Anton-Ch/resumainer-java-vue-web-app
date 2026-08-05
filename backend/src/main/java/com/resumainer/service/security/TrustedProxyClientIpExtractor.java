package com.resumainer.service.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.Inet6Address;
import java.util.Arrays;

/** Extracts a client IP without trusting forwarded headers from untrusted peers. */
@Component
public class TrustedProxyClientIpExtractor {

    private final String[] trustedProxyHosts;

    @Autowired
    public TrustedProxyClientIpExtractor(
            @Value("${app.auth.trusted-proxy-hosts:}") String trustedProxyHosts) {
        this.trustedProxyHosts = trustedProxyHosts == null ? new String[0]
                : Arrays.stream(trustedProxyHosts.split(","))
                .map(String::trim)
                .filter(host -> !host.isEmpty())
                .toArray(String[]::new);
    }

    /** Returns X-Real-IP only when the immediate peer is an explicitly trusted proxy. */
    public String extract(HttpServletRequest request) {
        String peer = request.getRemoteAddr();
        if (!isTrustedPeer(peer)) {
            return peer;
        }
        String realIp = request.getHeader("X-Real-IP");
        return isIpLiteral(realIp) ? realIp.trim() : peer;
    }

    private boolean isTrustedPeer(String peer) {
        for (String host : trustedProxyHosts) {
            try {
                for (InetAddress address : InetAddress.getAllByName(host)) {
                    if (address.getHostAddress().equals(peer)) {
                        return true;
                    }
                }
            } catch (Exception ignored) {
                // Fail closed: an unresolved configured proxy is not trusted.
            }
        }
        return false;
    }

    private static boolean isIpLiteral(String value) {
        if (value == null || value.isBlank() || value.length() > 45 || value.contains(" ")) {
            return false;
        }
        String candidate = value.trim();
        if (candidate.contains(":")) {
            try {
                return InetAddress.getByName(candidate) instanceof Inet6Address;
            } catch (Exception e) {
                return false;
            }
        }
        String[] parts = candidate.split("\\.", -1);
        if (parts.length != 4) {
            return false;
        }
        try {
            for (String part : parts) {
                if (!part.matches("[0-9]{1,3}") || Integer.parseInt(part) > 255) {
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

package com.resumainer.service.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrustedProxyClientIpExtractorTest {

    @Test
    void extract_blankTrustedProxyConfig_ignoresForwardedHeader() {
        TrustedProxyClientIpExtractor extractor = new TrustedProxyClientIpExtractor("  ");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.10");
        request.addHeader("X-Real-IP", "198.51.100.10");

        assertEquals("203.0.113.10", extractor.extract(request));
    }

    @Test
    void extract_untrustedImmediatePeer_ignoresSpoofedForwardedHeaders() {
        TrustedProxyClientIpExtractor extractor = new TrustedProxyClientIpExtractor("172.19.0.2");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.9");
        request.addHeader("X-Real-IP", "198.51.100.99");
        request.addHeader("X-Forwarded-For", "198.51.100.98");

        assertEquals("203.0.113.9", extractor.extract(request));
    }

    @Test
    void extract_trustedNginxPeer_usesNginxOverwrittenRealIp() {
        TrustedProxyClientIpExtractor extractor = new TrustedProxyClientIpExtractor("172.19.0.2");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("172.19.0.2");
        request.addHeader("X-Real-IP", "198.51.100.77");
        request.addHeader("X-Forwarded-For", "spoofed, 198.51.100.77");

        assertEquals("198.51.100.77", extractor.extract(request));
    }

    @Test
    void extract_trustedPeerWithInvalidRealIp_fallsBackToImmediatePeer() {
        TrustedProxyClientIpExtractor extractor = new TrustedProxyClientIpExtractor("172.19.0.2");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("172.19.0.2");
        request.addHeader("X-Real-IP", "not-an-ip");

        assertEquals("172.19.0.2", extractor.extract(request));
    }

    @Test
    void extract_trustedPeerWithBlankRealIp_fallsBackToImmediatePeer() {
        TrustedProxyClientIpExtractor extractor = new TrustedProxyClientIpExtractor("172.19.0.2");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("172.19.0.2");
        request.addHeader("X-Real-IP", "  ");

        assertEquals("172.19.0.2", extractor.extract(request));
    }

    @Test
    void extract_trustedPeerWithMalformedIpv6_fallsBackToImmediatePeer() {
        TrustedProxyClientIpExtractor extractor = new TrustedProxyClientIpExtractor("172.19.0.2");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("172.19.0.2");
        request.addHeader("X-Real-IP", "::::");

        assertEquals("172.19.0.2", extractor.extract(request));
    }

    @Test
    void extract_trustedPeerWithSignedIpv4Component_fallsBackToImmediatePeer() {
        TrustedProxyClientIpExtractor extractor = new TrustedProxyClientIpExtractor("172.19.0.2");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("172.19.0.2");
        request.addHeader("X-Real-IP", "+1.2.3.4");

        assertEquals("172.19.0.2", extractor.extract(request));
    }
}

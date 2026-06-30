package com.resumainer.service.security;

import com.resumainer.dao.UserDao;
import com.resumainer.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserDao userDao;

    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new CustomUserDetailsService(userDao);
    }

    private User createTestUser(long roleId, long statusId, boolean isDeleted,
                                 int failedAttempts, LocalDateTime lockedUntil,
                                 boolean emailVerified) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setPasswordHash("$2a$12$b6Flut1MIqFT5gQNqWZwtOWAIxbDDZHNW.tDRA4ppSCcZGHIXJTyG");
        user.setUsername("testuser");
        user.setRoleId(roleId);
        user.setStatusId(statusId);
        user.setPermissionId(1L);
        user.setPrivileged(false);
        user.setFailedLoginAttempts(failedAttempts);
        user.setLockedUntil(lockedUntil);
        user.setDeleted(isDeleted);
        user.setEmailVerified(emailVerified);
        user.setPasswordLoginEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    // ============================================================
    // T032 — Load user by email only (not username)
    // ============================================================

    @Test
    @DisplayName("T032: loadUserByUsername with email returns UserDetails")
    void loadUserByUsername_validEmail_returnsUserDetails() {
        User user = createTestUser(1L, 1L, false, 0, null, true);
        when(userDao.findByEmail("test@example.com")).thenReturn(user);

        CustomUserDetails userDetails =
                (CustomUserDetails) userDetailsService.loadUserByUsername("test@example.com");

        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertEquals(user.getPasswordHash(), userDetails.getPassword());
        assertEquals(user.getId(), userDetails.getUserId());
        assertEquals("testuser", userDetails.getDisplayName());
    }

    @Test
    @DisplayName("T032: verified email loads successfully, unverified throws DisabledException")
    void loadUserByUsername_verifiedEmail_returnsEnabledUser() {
        User user = createTestUser(1L, 1L, false, 0, null, true);
        when(userDao.findByEmail("test@example.com")).thenReturn(user);
        when(userDao.findByEmail("unverified@test.com")).thenReturn(
                createTestUser(1L, 1L, false, 0, null, false));

        assertDoesNotThrow(() -> userDetailsService.loadUserByUsername("test@example.com"));
        assertThrows(DisabledException.class,
                () -> userDetailsService.loadUserByUsername("unverified@test.com"));
    }

    @Test
    @DisplayName("T033: username (not email) throws UsernameNotFoundException")
    void loadUserByUsername_nonEmail_throwsException() {
        when(userDao.findByEmail("someusername")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("someusername"));
        verify(userDao).findByEmail("someusername");
    }

    // ============================================================
    // T037 — Role mapping: USER → ROLE_USER, ADMIN → ROLE_ADMIN
    // ============================================================

    @Test
    @DisplayName("T037: USER role maps to ROLE_USER authority")
    void userRole_mapsToRoleUser() {
        User user = createTestUser(1L, 1L, false, 0, null, true);
        when(userDao.findByEmail("test@example.com")).thenReturn(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername("test@example.com");
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")),
                "USER role must map to ROLE_USER");
        assertFalse(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")),
                "USER must not have ROLE_ADMIN");
    }

    @Test
    @DisplayName("T037: ADMIN role maps to ROLE_ADMIN authority")
    void adminRole_mapsToRoleAdmin() {
        User user = createTestUser(2L, 1L, false, 0, null, true);
        when(userDao.findByEmail("admin@test.com")).thenReturn(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername("admin@test.com");
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")),
                "ADMIN role must map to ROLE_ADMIN");
    }

    // ============================================================
    // T038 — Account state enforcement
    // ============================================================

    @Test
    @DisplayName("T038: deleted user throws UsernameNotFoundException")
    void deletedUser_throwsException() {
        User user = createTestUser(1L, 1L, true, 0, null, true);
        when(userDao.findByEmail("deleted@example.com")).thenReturn(user);

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("deleted@example.com"));
    }

    @Test
    @DisplayName("T038: blocked user (statusId != 1) throws UsernameNotFoundException")
    void blockedUser_throwsUsernameNotFoundException() {
        User user = createTestUser(1L, 2L, false, 0, null, true);
        when(userDao.findByEmail("blocked@example.com")).thenReturn(user);

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("blocked@example.com"));
    }

    @Test
    @DisplayName("T038: locked user throws LockedException")
    void lockedUser_throwsLockedException() {
        User user = createTestUser(1L, 1L, false, 5, LocalDateTime.now().plusHours(1), true);
        when(userDao.findByEmail("locked@example.com")).thenReturn(user);

        assertThrows(LockedException.class,
                () -> userDetailsService.loadUserByUsername("locked@example.com"));
    }

    @Test
    @DisplayName("T038: unverified user throws DisabledException")
    void unverifiedUser_throwsDisabledException() {
        User user = createTestUser(1L, 1L, false, 0, null, false);
        when(userDao.findByEmail("unverified@example.com")).thenReturn(user);

        assertThrows(DisabledException.class,
                () -> userDetailsService.loadUserByUsername("unverified@example.com"));
    }

    @Test
    @DisplayName("T038: password login disabled throws UsernameNotFoundException")
    void passwordLoginDisabled_throwsUsernameNotFoundException() {
        User user = createTestUser(1L, 1L, false, 0, null, true);
        user.setPasswordLoginEnabled(false);
        when(userDao.findByEmail("disabled@example.com")).thenReturn(user);

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("disabled@example.com"));
    }

    @Test
    @DisplayName("T038: non-existent email throws UsernameNotFoundException")
    void nonExistentEmail_throwsException() {
        when(userDao.findByEmail("unknown@example.com")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("unknown@example.com"));
    }

    // ============================================================
    // T039 — Password hash not exposed in toString/API
    // ============================================================

    @Test
    @DisplayName("T039: CustomUserDetails toString does not expose password hash")
    void toString_doesNotExposePasswordHash() {
        User user = createTestUser(1L, 1L, false, 0, null, true);
        when(userDao.findByEmail("test@example.com")).thenReturn(user);

        CustomUserDetails userDetails =
                (CustomUserDetails) userDetailsService.loadUserByUsername("test@example.com");

        String toString = userDetails.toString();
        assertFalse(toString.contains(user.getPasswordHash()),
                "toString must not contain password hash");
        assertFalse(toString.contains("$2a$"),
                "toString must not contain BCrypt hash pattern");
    }

    // ============================================================
    // T040 — Verify migrated user can authenticate
    // ============================================================

    @Test
    @DisplayName("T040: active verified user returns enabled UserDetails")
    void activeVerifiedUser_returnsEnabledUserDetails() {
        User user = createTestUser(1L, 1L, false, 0, null, true);
        when(userDao.findByEmail("test@example.com")).thenReturn(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername("test@example.com");

        assertTrue(userDetails.isEnabled(), "Active verified user must be enabled");
        assertTrue(userDetails.isAccountNonExpired(), "Active user must have non-expired account");
        assertTrue(userDetails.isCredentialsNonExpired(), "Active user must have non-expired credentials");
        assertTrue(userDetails.isAccountNonLocked(), "Active user must have non-locked account");
    }
}

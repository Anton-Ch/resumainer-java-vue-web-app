package com.resumainer.service.security;

import com.resumainer.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Spring Security {@link UserDetails} wrapper around the project's {@link User} model.
 *
 * <p>Exposes userId, email, display name, and role-based authorities.
 * Password hash is exposed for authentication but excluded from {@code toString()}.
 */
public class CustomUserDetails implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final UUID userId;
    private final String email;
    private final String displayName;
    private final String passwordHash;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean privileged;
    private final boolean enabled;
    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;

    public CustomUserDetails(User user, long roleId) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.displayName = user.getUsername();
        this.passwordHash = user.getPasswordHash();
        this.privileged = user.isPrivileged();

        String roleName = roleId == 2L ? "ROLE_ADMIN" : "ROLE_USER";
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority(roleName));

        // enabled: not deleted, active status (statusId=1), email verified, password login enabled
        this.enabled = !user.isDeleted()
                && user.getStatusId() == 1L
                && user.isEmailVerified()
                && user.isPasswordLoginEnabled();

        // accountNonLocked: not locked (null lockedUntil or expired)
        this.accountNonLocked = user.getLockedUntil() == null
                || user.getLockedUntil().isBefore(java.time.LocalDateTime.now());

        // accountNonExpired: account not expired (not relevant for this app)
        this.accountNonExpired = true;

        // credentialsNonExpired: password not expired (not relevant for this app)
        this.credentialsNonExpired = true;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isPrivileged() {
        return privileged;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public String toString() {
        return "CustomUserDetails{userId=" + userId
                + ", email='" + email + '\''
                + ", displayName='" + displayName + '\''
                + ", authorities=" + authorities
                + ", enabled=" + enabled
                + ", privileged=" + privileged
                + '}';
    }
}

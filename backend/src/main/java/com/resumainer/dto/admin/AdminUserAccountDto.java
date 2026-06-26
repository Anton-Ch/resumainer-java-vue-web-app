package com.resumainer.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Account section for admin user details.
 * accountEmail comes from users.email (read-only display).
 * Role/status/permission codes are used, not hardcoded IDs.
 */
public class AdminUserAccountDto {

    private String id;
    private String username;
    private String accountEmail;
    private String roleCode;
    private String roleName;
    private String statusCode;
    private String statusName;
    private String permissionCode;
    private String permissionName;
    private boolean isPrivileged;
    private String defaultLanguageCode;
    private String defaultLanguageName;
    private String secondaryLanguageCode;
    private String secondaryLanguageName;
    private String createdAt;
    private String updatedAt;

    public AdminUserAccountDto() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAccountEmail() { return accountEmail; }
    public void setAccountEmail(String accountEmail) { this.accountEmail = accountEmail; }

    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }

    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }

    public String getPermissionCode() { return permissionCode; }
    public void setPermissionCode(String permissionCode) { this.permissionCode = permissionCode; }

    public String getPermissionName() { return permissionName; }
    public void setPermissionName(String permissionName) { this.permissionName = permissionName; }

    @JsonProperty("isPrivileged")
    public boolean isPrivileged() { return isPrivileged; }
    public void setPrivileged(boolean privileged) { isPrivileged = privileged; }

    public String getDefaultLanguageCode() { return defaultLanguageCode; }
    public void setDefaultLanguageCode(String defaultLanguageCode) { this.defaultLanguageCode = defaultLanguageCode; }

    public String getDefaultLanguageName() { return defaultLanguageName; }
    public void setDefaultLanguageName(String defaultLanguageName) { this.defaultLanguageName = defaultLanguageName; }

    public String getSecondaryLanguageCode() { return secondaryLanguageCode; }
    public void setSecondaryLanguageCode(String secondaryLanguageCode) { this.secondaryLanguageCode = secondaryLanguageCode; }

    public String getSecondaryLanguageName() { return secondaryLanguageName; }
    public void setSecondaryLanguageName(String secondaryLanguageName) { this.secondaryLanguageName = secondaryLanguageName; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}

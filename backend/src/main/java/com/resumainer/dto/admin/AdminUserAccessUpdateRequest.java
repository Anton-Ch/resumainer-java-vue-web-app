package com.resumainer.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for PATCH /api/admin/users/{userId}/access.
 * Only access-control fields; no account email, password, or profile fields.
 */
public class AdminUserAccessUpdateRequest {

    private String roleCode;
    private String statusCode;
    private String permissionCode;
    private boolean isPrivileged;

    public AdminUserAccessUpdateRequest() {
    }

    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }

    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }

    public String getPermissionCode() { return permissionCode; }
    public void setPermissionCode(String permissionCode) { this.permissionCode = permissionCode; }

    @JsonProperty("isPrivileged")
    public boolean isPrivileged() { return isPrivileged; }
    public void setPrivileged(boolean privileged) { isPrivileged = privileged; }
}

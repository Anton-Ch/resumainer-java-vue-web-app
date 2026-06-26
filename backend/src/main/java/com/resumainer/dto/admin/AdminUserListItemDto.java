package com.resumainer.dto.admin;

/**
 * Admin user table row DTO.
 * <p>
 * Contains user identity, role/status/permission info, and resume count.
 * <p>
 * Security: must not expose password_hash, API keys, or internal paths.
 */
public class AdminUserListItemDto {

    private String id;
    private String fullName;
    private String username;
    private String email;
    private String roleCode;
    private String roleName;
    private String statusCode;
    private String statusName;
    private String permissionCode;
    private String permissionName;
    private boolean isPrivileged;
    private long resumesCount;
    private String createdAt;

    public AdminUserListItemDto() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

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

    public boolean isPrivileged() { return isPrivileged; }
    public void setPrivileged(boolean privileged) { isPrivileged = privileged; }

    public long getResumesCount() { return resumesCount; }
    public void setResumesCount(long resumesCount) { this.resumesCount = resumesCount; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}

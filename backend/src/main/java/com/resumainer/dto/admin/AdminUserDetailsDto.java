package com.resumainer.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Composed admin user details response.
 * Contains account, contacts, and additional info sections.
 * <p>
 * isCurrentAdmin is computed from session, never from request params.
 */
public class AdminUserDetailsDto {

    private String id;
    private boolean isCurrentAdmin;
    private AdminUserAccountDto account;
    private AdminUserContactDto contacts;
    private AdminUserAdditionalInfoDto additionalInfo;

    public AdminUserDetailsDto() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @JsonProperty("isCurrentAdmin")
    public boolean isCurrentAdmin() { return isCurrentAdmin; }
    public void setCurrentAdmin(boolean currentAdmin) { isCurrentAdmin = currentAdmin; }

    public AdminUserAccountDto getAccount() { return account; }
    public void setAccount(AdminUserAccountDto account) { this.account = account; }

    public AdminUserContactDto getContacts() { return contacts; }
    public void setContacts(AdminUserContactDto contacts) { this.contacts = contacts; }

    public AdminUserAdditionalInfoDto getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(AdminUserAdditionalInfoDto additionalInfo) { this.additionalInfo = additionalInfo; }
}

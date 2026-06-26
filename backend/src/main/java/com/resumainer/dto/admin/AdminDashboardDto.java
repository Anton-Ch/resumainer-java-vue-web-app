package com.resumainer.dto.admin;

/**
 * Dashboard summary for Admin Home.
 */
public class AdminDashboardDto {

    private long totalUsers;
    private long totalResumes;
    private long totalTokensSent;
    private boolean totalTokensSentWip;
    private long totalTokensGenerated;
    private boolean totalTokensGeneratedWip;

    public AdminDashboardDto() {
    }

    public AdminDashboardDto(long totalUsers, long totalResumes) {
        this.totalUsers = totalUsers;
        this.totalResumes = totalResumes;
        this.totalTokensSent = 0;
        this.totalTokensSentWip = true;
        this.totalTokensGenerated = 0;
        this.totalTokensGeneratedWip = true;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalResumes() {
        return totalResumes;
    }

    public void setTotalResumes(long totalResumes) {
        this.totalResumes = totalResumes;
    }

    public long getTotalTokensSent() {
        return totalTokensSent;
    }

    public void setTotalTokensSent(long totalTokensSent) {
        this.totalTokensSent = totalTokensSent;
    }

    public boolean isTotalTokensSentWip() {
        return totalTokensSentWip;
    }

    public void setTotalTokensSentWip(boolean totalTokensSentWip) {
        this.totalTokensSentWip = totalTokensSentWip;
    }

    public long getTotalTokensGenerated() {
        return totalTokensGenerated;
    }

    public void setTotalTokensGenerated(long totalTokensGenerated) {
        this.totalTokensGenerated = totalTokensGenerated;
    }

    public boolean isTotalTokensGeneratedWip() {
        return totalTokensGeneratedWip;
    }

    public void setTotalTokensGeneratedWip(boolean totalTokensGeneratedWip) {
        this.totalTokensGeneratedWip = totalTokensGeneratedWip;
    }
}

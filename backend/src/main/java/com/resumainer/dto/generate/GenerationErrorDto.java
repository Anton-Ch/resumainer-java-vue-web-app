package com.resumainer.dto.generate;

/**
 * Structured error response for generation failures.
 * Frontend uses retryAllowed and changeSettingsAllowed to show appropriate actions.
 */
public class GenerationErrorDto {

    private String errorCode;
    private String message;
    private boolean retryAllowed;
    private boolean changeSettingsAllowed;
    private String requestStatus;

    public GenerationErrorDto() {}

    public GenerationErrorDto(String errorCode, String message,
                               boolean retryAllowed, boolean changeSettingsAllowed,
                               String requestStatus) {
        this.errorCode = errorCode;
        this.message = message;
        this.retryAllowed = retryAllowed;
        this.changeSettingsAllowed = changeSettingsAllowed;
        this.requestStatus = requestStatus;
    }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRetryAllowed() { return retryAllowed; }
    public void setRetryAllowed(boolean retryAllowed) { this.retryAllowed = retryAllowed; }

    public boolean isChangeSettingsAllowed() { return changeSettingsAllowed; }
    public void setChangeSettingsAllowed(boolean changeSettingsAllowed) { this.changeSettingsAllowed = changeSettingsAllowed; }

    public String getRequestStatus() { return requestStatus; }
    public void setRequestStatus(String requestStatus) { this.requestStatus = requestStatus; }
}

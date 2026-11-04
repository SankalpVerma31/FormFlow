package com.formaccess.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordForm {

    @NotBlank(message = "Enter your current password")
    private String currentPassword;

    @NotBlank
    @Size(min = 8, max = 72, message = "New password must be 8–72 characters")
    private String newPassword;

    @NotBlank
    private String confirmPassword;

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String v) { this.currentPassword = v; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String v) { this.newPassword = v; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String v) { this.confirmPassword = v; }
}

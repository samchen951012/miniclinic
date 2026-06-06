package tw.edu.fju.miniclinic.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordForm {

    @NotBlank(message = "請輸入舊密碼")
    private String oldPassword;

    @NotBlank(message = "請輸入新密碼")
    @Size(min = 8, message = "新密碼長度至少需要 8 個字元") // ✨ 滿足講義至少 8 碼的要求
    private String newPassword;

    @NotBlank(message = "請再次輸入新密碼以供確認")
    private String confirmPassword;

    // --- Getters and Setters ---
    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
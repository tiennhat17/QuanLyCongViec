package com.ccnlthd.taskmanager.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
public class RegisterRequest {
    @NotBlank(message = "Tên đăng nhập 1–50 ký tự")
    @Size(max = 50, message = "Tên đăng nhập 1–50 ký tự")
    private String username;

    @NotBlank(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email tối đa 100 ký tự")
    @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu tối thiểu 6 ký tự")
    @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự")
    private String password;

    @Setter
    private String confirm;

    @Setter
    @Min(value = -840, message = "Múi giờ không hợp lệ")
    @Max(value = 840, message = "Múi giờ không hợp lệ")
    private int clientTimezoneOffset;

    public void setUsername(String username) {
        this.username = username == null ? null : username.trim();
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @AssertTrue(message = "Mật khẩu xác nhận không khớp")
    public boolean isPasswordConfirmed() {
        return password == null || password.equals(confirm);
    }
}

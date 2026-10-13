package com.ccnlthd.taskmanager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class LoginRequest {
    @NotBlank(message = "Vui lòng nhập tên đăng nhập hoặc email")
    @Size(max = 100, message = "Tên đăng nhập hoặc email tối đa 100 ký tự")
    private String identity;

    @NotBlank(message = "Vui lòng nhập mật khẩu")
    private String password;

    public void setIdentity(String identity) {
        this.identity = identity == null ? null : identity.trim();
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

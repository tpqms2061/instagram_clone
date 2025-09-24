package com.ssh.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class AuthRequest {
    // 사용자명 (옵션 - 이메일 대신 가능)
    private String username;

    // 이메일 (옵션 - username 대신 가능)
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
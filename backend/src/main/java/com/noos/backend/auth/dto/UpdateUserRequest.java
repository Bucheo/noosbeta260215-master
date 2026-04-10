package com.noos.backend.auth.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String displayName;
    private String password;  // 새 비밀번호 (빈 값이면 변경 안 함)
    private String role;
}
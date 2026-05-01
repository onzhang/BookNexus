package com.zjw.booknexus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResp {
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String username;
    private String role;
}

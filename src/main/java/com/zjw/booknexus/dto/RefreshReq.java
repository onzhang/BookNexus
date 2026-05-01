package com.zjw.booknexus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshReq {

    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}

package com.hbpu.smartpicture.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 接收用户登录请求的封装类
 */
@Schema(description = "接收用户登录请求的封装类")
@Data
public class UserLoginDTO implements Serializable {
    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = -6030480081854163165L;

    /**
     * 账号
     */
    @Schema(description = "账号")
    private String userAccount;

    /**
     * 密码
     */
    @Schema(description = "密码")
    private String userPassword;
}

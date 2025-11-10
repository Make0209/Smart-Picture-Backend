package com.hbpu.smartpicture.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户注册请求封装类
 */
@Schema(description = "用户注册请求封装类")
@Data
public class UserRegisterDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -416359191083494243L;

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

    /**
     * 确认密码
     */
    @Schema(description = "确认密码")
    private String checkPassword;
}

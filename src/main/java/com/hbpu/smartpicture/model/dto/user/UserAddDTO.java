package com.hbpu.smartpicture.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理员创建用户的请求封装类
 */
@Schema(description = "管理员创建用户的请求封装类")
@Data
public class UserAddDTO implements Serializable {
    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = -8342935341664040825L;

    /**
     * 账号
     */
    @Schema(description = "账号")
    private String userAccount;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    private String userName;

    /**
     * 用户头像
     */
    @Schema(description = "用户头像")
    private String userAvatar;

    /**
     * 用户简介
     */
    @Schema(description = "用户简介")
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    @Schema(description = "用户角色：user/admin")
    private String userRole;
}

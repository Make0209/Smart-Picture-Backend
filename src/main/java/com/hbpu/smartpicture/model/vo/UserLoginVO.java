package com.hbpu.smartpicture.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**]
 * 用户登录响应封装类
 */
@Schema(description = "用户登录响应封装类")
@Data
public class UserLoginVO implements Serializable {
    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = -9041387633841720571L;

    /**
     * id
     */
    @Schema(description = "id")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

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

    /**
     * 用户token
     */
    @Schema(description = "用户token")
    private String token;
}

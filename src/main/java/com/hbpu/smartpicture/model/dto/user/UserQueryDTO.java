package com.hbpu.smartpicture.model.dto.user;

import com.hbpu.smartpicture.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 接收搜索参数的分页查询请求封装类
 */
@Schema(description = "接收搜索参数的分页查询请求封装类")
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryDTO extends PageRequest implements Serializable {
    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = -1177494805128299513L;

    /**
     * id
     */
    @Schema(description = "id")
    private Long id;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    private String userName;

    /**
     * 账号
     */
    @Schema(description = "账号")
    private String userAccount;

    /**
     * 简介
     */
    @Schema(description = "简介")
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    @Schema(description = "用户角色：user/admin/ban")
    private String userRole;

}

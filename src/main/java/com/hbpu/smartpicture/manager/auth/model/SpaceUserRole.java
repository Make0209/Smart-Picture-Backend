package com.hbpu.smartpicture.manager.auth.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 空间用户角色实体类
 * <p>
 * 【通俗解释】
 * 这个类代表一个"角色"，角色是一组权限的集合。
 * 可以把角色理解为"你是什么身份"，比如"管理员"、"编辑者"、"浏览者"。
 * <p>
 * 【举个例子】
 * - key: "admin"                              → 角色标识
 * - name: "管理员"                             → 角色名称
 * - permissions: ["spaceUser:manage", "picture:view", "picture:upload", ...] → 拥有的权限列表
 * - description: "成员管理、查看图片、上传图片..." → 角色描述
 * <p>
 * 【类比理解】
 * 继续用公司门禁卡的例子：
 * - "普通员工"角色 = 刷大门权限
 * - "部门经理"角色 = 刷大门 + 办公室 + 会议室权限
 * - "总经理"角色   = 所有门禁权限
 * <p>
 * 这里的 SpaceUserRole 就是定义"普通员工"这个角色包含哪些权限
 * <p>
 * 【系统中的三个角色】
 * - viewer（浏览者）: 只能看图片
 * - editor（编辑者）: 能看、上传、编辑、删除图片
 * - admin（管理员） : 能管理成员 + editor的所有权限
 *
 * @see SpaceUserPermission 角色包含的具体权限
 * @see SpaceUserAuthConfig 所有权限和角色的配置中心
 */
@Schema(description = "空间用户角色")
@Data
public class SpaceUserRole implements Serializable {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = 8257951331000747536L;

    /**
     * 角色键（唯一标识）
     * <p>
     * 系统中预定义了三个角色：
     * - "viewer"  → 浏览者，只能查看图片
     * - "editor"  → 编辑者，可以管理图片（增删改查）
     * - "admin"   → 管理员，可以管理成员 + 管理图片
     * <p>
     * 这个key会存储在数据库的 space_user 表的 space_role 字段中
     */
    @Schema(description = "角色键")
    private String key;

    /**
     * 角色名称（展示用）
     * <p>
     * 给用户看的友好名称，比如"浏览者"、"编辑者"、"管理员"
     * 会在前端界面、成员管理页面等地方显示
     */
    @Schema(description = "角色名称")
    private String name;

    /**
     * 权限键列表
     * <p>
     * 这个角色拥有的所有权限，存储的是 SpaceUserPermission 的 key 列表
     * <p>
     * 例如 admin 角色的 permissions：
     * ["spaceUser:manage", "picture:view", "picture:upload", "picture:edit", "picture:delete"]
     * <p>
     * 当判断用户是否有某个权限时，系统会：
     * 1. 查出用户的角色（如 admin）
     * 2. 找到该角色的权限列表
     * 3. 检查目标权限是否在列表中
     */
    @Schema(description = "权限键列表")
    private List<String> permissions;

    /**
     * 角色描述（详细说明）
     * <p>
     * 描述这个角色具体能做什么，比如：
     * "查看图片"、"查看图片、上传图片、修改图片、删除图片"
     * 用于帮助空间管理员理解每个角色的能力范围
     */
    @Schema(description = "角色描述")
    private String description;

}

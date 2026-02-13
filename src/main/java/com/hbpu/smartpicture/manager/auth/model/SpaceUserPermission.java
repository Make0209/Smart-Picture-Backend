package com.hbpu.smartpicture.manager.auth.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 空间用户权限实体类
 * <p>
 * 【通俗解释】
 * 这个类代表一个具体的"权限"，比如"查看图片"、"删除图片"、"添加成员"等。
 * 可以把权限理解为"你能做什么"，是系统中最细粒度的操作控制单位。
 * <p>
 * 【举个例子】
 * - key: "picture:view"      → 表示"查看图片"这个权限
 * - name: "查看图片"          → 给用户看的友好名称
 * - description: "查看空间中的图片内容" → 详细说明这个权限能干什么
 * <p>
 * 【类比理解】
 * 就像公司里的门禁卡权限：
 * - 门禁卡A可以刷开大门（一个权限）
 * - 门禁卡B可以刷开大门+办公室（两个权限）
 * 这里的 SpaceUserPermission 就是定义"刷开大门"这个权限本身
 *
 * @see SpaceUserRole 权限会被组合成角色
 * @see SpaceUserAuthConfig 所有权限和角色的配置中心
 */
@Schema(description = "空间用户权限")
@Data
public class SpaceUserPermission implements Serializable {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = 6264714615873246124L;

    /**
     * 权限键（唯一标识）
     * <p>
     * 格式通常采用 "资源:操作" 的形式，例如：
     * - picture:view    → 查看图片
     * - picture:upload  → 上传图片
     * - picture:delete  → 删除图片
     * - spaceUser:manage → 管理成员
     * <p>
     * 这个key在代码中会用于判断用户是否有某个具体操作的权利
     */
    @Schema(description = "权限键")
    private String key;

    /**
     * 权限名称（展示用）
     * <p>
     * 给用户看的友好名称，比如"查看图片"、"上传图片"
     * 会在前端界面、权限管理页面等地方显示
     */
    @Schema(description = "权限名称")
    private String name;

    /**
     * 权限描述（详细说明）
     * <p>
     * 更详细的描述这个权限具体能做什么，比如：
     * "查看空间中的图片内容"、"上传图片到空间中"
     * 用于帮助管理员理解每个权限的具体作用
     */
    @Schema(description = "权限描述")
    private String description;

}

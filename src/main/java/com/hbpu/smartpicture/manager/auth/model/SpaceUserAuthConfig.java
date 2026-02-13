package com.hbpu.smartpicture.manager.auth.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 空间用户权限配置实体类（配置中心）
 * <p>
 * 【通俗解释】
 * 这个类是整个权限系统的"配置中心"，相当于一个容器，里面装了所有的权限定义和角色定义。
 * 它对应 JSON 配置文件 SpaceUserAuthConfig.json，系统启动时会加载这个配置。
 * <p>
 * 【举个例子】
 * 就像一家公司的规章制度手册：
 * - 手册里列出了所有"权限"（能做什么）：使用打印机、预订会议室、审批报销...
 * - 手册里定义了所有"角色"（职位）：实习生、正式员工、部门经理...
 * - 每个角色对应哪些权限：实习生只能用打印机，部门经理什么都能做
 * <p>
 * 【在系统中的作用】
 * 1. 系统启动时，从 SpaceUserAuthConfig.json 加载配置到这个对象
 * 2. 当需要判断用户是否有某个权限时，从这个配置中查找角色对应的权限列表
 * 3. 前端展示角色和权限时，也从这个配置获取定义信息
 * <p>
 * 【JSON配置文件对应关系】
 * SpaceUserAuthConfig.json 的内容会被反序列化成这个类的对象：
 * - json中的 "permissions" 数组 → permissions 字段 (List&lt;SpaceUserPermission&gt;)
 * - json中的 "roles" 数组       → roles 字段 (List&lt;SpaceUserRole&gt;)
 *
 * @see SpaceUserPermission 权限定义
 * @see SpaceUserRole 角色定义
 * @see /biz/SpaceUserAuthConfig.json 对应的配置文件
 */
@Schema(description = "空间用户权限配置")
@Data
public class SpaceUserAuthConfig implements Serializable {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = -3834470620645468550L;

    /**
     * 权限列表
     * <p>
     * 系统中定义的所有权限，对应 JSON 中的 "permissions" 数组
     * <p>
     * 当前系统定义了5个权限：
     * - spaceUser:manage → 成员管理（添加或移除成员）
     * - picture:view     → 查看图片
     * - picture:upload   → 上传图片
     * - picture:edit     → 修改图片
     * - picture:delete   → 删除图片
     * <p>
     * 这些权限是系统中最细粒度的操作控制单位
     */
    @Schema(description = "权限列表")
    private List<SpaceUserPermission> permissions;

    /**
     * 角色列表
     * <p>
     * 系统中定义的所有角色，对应 JSON 中的 "roles" 数组
     * <p>
     * 当前系统定义了3个角色：
     * - viewer（浏览者）: 只有 picture:view 权限
     * - editor（编辑者）: 有 picture:view/upload/edit/delete 权限
     * - admin（管理员） : 有所有权限（包括 spaceUser:manage）
     * <p>
     * 角色是权限的集合，用户被分配某个角色后，就拥有了该角色的所有权限
     */
    @Schema(description = "角色列表")
    private List<SpaceUserRole> roles;


}

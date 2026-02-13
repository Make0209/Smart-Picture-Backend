package com.hbpu.smartpicture.manager.auth.model;

/**
 * 空间用户权限常量类
 * <p>
 * 【通俗解释】
 * 这个类定义了系统中所有权限的常量字符串，方便在代码中直接使用，避免手写字符串出错。
 * <p>
 * 【举个例子】
 * 不用常量：
 * <pre>
 * if (user.hasPermission("picture:view")) {  // 手写字符串，容易拼错
 *     // ...
 * }
 * </pre>
 * <p>
 * 使用常量：
 * <pre>
 * if (user.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW)) {  // 类型安全，有IDE提示
 *     // ...
 * }
 * </pre>
 * <p>
 * 【当前定义的权限常量】
 * 对应 SpaceUserAuthConfig.json 中的 permissions 数组
 *
 * @see SpaceUserPermission 权限实体类
 * @see SpaceUserAuthConfig 权限配置中心
 */
public interface SpaceUserPermissionConstant {

    /**
     * 成员管理权限
     * <p>
     * 拥有此权限的用户可以：
     * - 邀请新成员加入空间
     * - 移除空间中的成员
     * - 修改成员的角色
     * <p>
     * 【注意】只有 admin 角色拥有此权限
     */
    String SPACE_USER_MANAGE = "spaceUser:manage";

    /**
     * 查看图片权限
     * <p>
     * 拥有此权限的用户可以：
     * - 浏览空间中的图片
     * - 查看图片详情
     * <p>
     * 【注意】所有角色（viewer/editor/admin）都拥有此权限
     */
    String PICTURE_VIEW = "picture:view";

    /**
     * 上传图片权限
     * <p>
     * 拥有此权限的用户可以：
     * - 上传新图片到空间
     * - 批量上传图片
     * <p>
     * 【注意】editor 和 admin 角色拥有此权限，viewer 没有
     */
    String PICTURE_UPLOAD = "picture:upload";

    /**
     * 修改图片权限
     * <p>
     * 拥有此权限的用户可以：
     * - 编辑图片信息（名称、标签等）
     * - 修改图片分类
     * <p>
     * 【注意】editor 和 admin 角色拥有此权限，viewer 没有
     */
    String PICTURE_EDIT = "picture:edit";

    /**
     * 删除图片权限
     * <p>
     * 拥有此权限的用户可以：
     * - 删除空间中的图片
     * - 批量删除图片
     * <p>
     * 【注意】editor 和 admin 角色拥有此权限，viewer 没有
     */
    String PICTURE_DELETE = "picture:delete";

}

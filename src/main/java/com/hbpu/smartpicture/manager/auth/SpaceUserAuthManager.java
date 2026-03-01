package com.hbpu.smartpicture.manager.auth;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hbpu.smartpicture.manager.auth.model.SpaceUserAuthConfig;
import com.hbpu.smartpicture.manager.auth.model.SpaceUserRole;
import com.hbpu.smartpicture.model.enums.SpaceRoleEnum;
import com.hbpu.smartpicture.model.enums.SpaceTypeEnum;
import com.hbpu.smartpicture.model.pojo.Space;
import com.hbpu.smartpicture.model.pojo.SpaceUser;
import com.hbpu.smartpicture.model.pojo.User;
import com.hbpu.smartpicture.service.SpaceUserService;
import com.hbpu.smartpicture.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 空间用户权限管理器
 */
@Component
public class SpaceUserAuthManager {

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private UserService userService;

    public static final SpaceUserAuthConfig SPACE_USER_AUTH_CONFIG;

    /*
      静态代码块：在类加载时执行，用于初始化空间用户权限配置
      从资源文件中读取JSON配置并转换为Java对象
     */
    static {
        String json = ResourceUtil.readUtf8Str("biz/SpaceUserAuthConfig.json");
        SPACE_USER_AUTH_CONFIG = JSONUtil.toBean(json, SpaceUserAuthConfig.class);
    }

    /**
     * 根据角色获取权限列表
     *
     * @param spaceUserRole 空间用户角色标识（如：admin、member等）
     * @return 返回该角色拥有的权限列表，如果角色为空或不存在则返回空列表
     */
    public List<String> getPermissionsByRole(String spaceUserRole) {
        if (StrUtil.isBlank(spaceUserRole)) {
            return new ArrayList<>();
        }
        SpaceUserRole role = SPACE_USER_AUTH_CONFIG.getRoles().stream()
                                                   .filter(r -> spaceUserRole.equals(r.getKey()))
                                                   .findFirst()
                                                   .orElse(null);
        if (role == null) {
            return new ArrayList<>();
        }
        return role.getPermissions();
    }

    /**
     * 获取空间权限列表（通过 HttpServletRequest，适用于普通 HTTP 请求）
     *
     * @param space   空间对象
     * @param request 用户请求
     * @return 返回空间的权限列表
     */
    public List<String> getPermissionList(Space space, HttpServletRequest request) {
        // 从 request 中解析当前登录用户，再委托给重载方法
        User loginUser = userService.getCurrentUser(request);
        return getPermissionList(space, loginUser);
    }

    /**
     * 获取空间权限列表（直接传入 User，适用于 WebSocket 握手等无法读取请求头的场景）
     *
     * @param space     空间对象
     * @param loginUser 已解析好的登录用户
     * @return 返回空间的权限列表，如果用户为空则返回空列表
     */
    public List<String> getPermissionList(Space space, User loginUser) {
        if (loginUser == null) {
            return new ArrayList<>();
        }
        List<String> ADMIN_PERMISSIONS = getPermissionsByRole(SpaceRoleEnum.ADMIN.getValue());
        // 公共图库（无空间）
        if (space == null) {
            if (userService.isAdmin(loginUser.getId())) {
                return ADMIN_PERMISSIONS;
            }
            return new ArrayList<>();
        }
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(space.getSpaceType());
        if (spaceTypeEnum == null) {
            return new ArrayList<>();
        }
        // 根据空间类型获取对应的权限
        switch (spaceTypeEnum) {
            case PRIVATE:
                // 私有空间，仅本人或管理员有所有权限
                if (space.getUserId().equals(loginUser.getId()) || userService.isAdmin(loginUser.getId())) {
                    return ADMIN_PERMISSIONS;
                } else {
                    return new ArrayList<>();
                }
            case TEAM:
                // 团队空间，查询 SpaceUser 并获取角色和权限
                SpaceUser spaceUser = spaceUserService.lambdaQuery()
                                                      .eq(SpaceUser::getSpaceId, space.getId())
                                                      .eq(SpaceUser::getUserId, loginUser.getId())
                                                      .one();
                if (spaceUser == null) {
                    return new ArrayList<>();
                } else {
                    return getPermissionsByRole(spaceUser.getSpaceRole());
                }
        }
        return new ArrayList<>();
    }

}
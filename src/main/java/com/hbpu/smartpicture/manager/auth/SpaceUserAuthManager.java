package com.hbpu.smartpicture.manager.auth;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hbpu.smartpicture.manager.auth.model.SpaceUserAuthConfig;
import com.hbpu.smartpicture.manager.auth.model.SpaceUserRole;
import com.hbpu.smartpicture.service.SpaceUserService;
import com.hbpu.smartpicture.service.UserService;
import jakarta.annotation.Resource;
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
        // 从类路径下的 biz/SpaceUserAuthConfig.json 文件中读取JSON配置内容
        String json = ResourceUtil.readUtf8Str("biz/SpaceUserAuthConfig.json");
        // 将JSON字符串转换为 SpaceUserAuthConfig 配置对象，并赋值给静态常量
        SPACE_USER_AUTH_CONFIG = JSONUtil.toBean(json, SpaceUserAuthConfig.class);
    }

    /**
     * 根据角色获取权限列表
     *
     * @param spaceUserRole 空间用户角色标识（如：admin、member等）
     * @return 返回该角色拥有的权限列表，如果角色为空或不存在则返回空列表
     */
    public List<String> getPermissionsByRole(String spaceUserRole) {
        // 判断传入的角色标识是否为空，如果为空则直接返回空列表
        if (StrUtil.isBlank(spaceUserRole)) {
            return new ArrayList<>();
        }
        // 从配置中查找匹配的角色对象
        // 使用Stream流遍历所有角色，通过filter过滤出key与传入角色标识相同的角色
        SpaceUserRole role = SPACE_USER_AUTH_CONFIG.getRoles().stream()
                                                   .filter(r -> spaceUserRole.equals(r.getKey()))  // 过滤条件：角色key相等
                                                   .findFirst()  // 获取第一个匹配的角色
                                                   .orElse(null);  // 如果没找到则返回null
        // 如果没有找到匹配的角色，返回空列表
        if (role == null) {
            return new ArrayList<>();
        }
        // 返回该角色对应的权限列表
        return role.getPermissions();
    }
}

package com.hbpu.smartpicture.manager.websocket;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.hbpu.smartpicture.manager.auth.SpaceUserAuthManager;
import com.hbpu.smartpicture.manager.auth.model.SpaceUserPermissionConstant;
import com.hbpu.smartpicture.model.enums.SpaceTypeEnum;
import com.hbpu.smartpicture.model.pojo.Picture;
import com.hbpu.smartpicture.model.pojo.Space;
import com.hbpu.smartpicture.model.pojo.User;
import com.hbpu.smartpicture.security.JwtUtil;
import com.hbpu.smartpicture.service.PictureService;
import com.hbpu.smartpicture.service.SpaceService;
import com.hbpu.smartpicture.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.redisson.api.RedissonClient;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

/**
 * WebSocket 拦截器，建立连接前要先校验
 */
@Slf4j
@Component
public class WsHandshakeInterceptor implements HandshakeInterceptor {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    @Resource
    private RedissonClient redisson;

    /**
     * 建立连接前要先校验
     *
     * @param request
     * @param response
     * @param wsHandler
     * @param attributes 给 WebSocketSession 会话设置属性
     * @return
     */
    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpServletRequest = servletRequest.getServletRequest();

            // 获取 pictureId 参数
            String pictureId = httpServletRequest.getParameter("pictureId");
            if (StrUtil.isBlank(pictureId)) {
                log.error("缺少图片参数，拒绝握手");
                return false;
            }

            // ★ 核心修改：WebSocket 无法携带自定义请求头
            //   改为从 URL 查询参数中获取 token，再手动解析
            String token = httpServletRequest.getParameter("token");
            if (StrUtil.isBlank(token)) {
                log.error("缺少 token 参数，拒绝握手");
                return false;
            }

            // 校验 token 是否过期
            try {
                if (JwtUtil.isTokenExpired(token)) {
                    log.error("token 已过期，拒绝握手");
                    return false;
                }
            } catch (Exception e) {
                log.error("token 无效，拒绝握手", e);
                return false;
            }

            // 从 token 中解析出用户账号，再从 Redis 中获取用户对象
            String userAccount = JwtUtil.getUserAccount(token);
            String redisKey = String.format("smart-picture:user:login:token:%s", userAccount);
            User loginUser = (User) redisson.getMapCache(redisKey).get("object");

            if (ObjUtil.isEmpty(loginUser)) {
                log.error("用户未登录或 token 已失效，拒绝握手");
                return false;
            }

            // 后续图片和空间权限校验逻辑保持不变
            Picture picture = pictureService.getById(pictureId);
            if (ObjUtil.isEmpty(picture)) {
                log.error("图片不存在，拒绝握手");
                return false;
            }
            Long spaceId = picture.getSpaceId();
            Space space = null;
            if (spaceId != null) {
                space = spaceService.getById(spaceId);
                if (ObjUtil.isEmpty(space)) {
                    log.error("图片所在空间不存在，拒绝握手");
                    return false;
                }
                if (space.getSpaceType() != SpaceTypeEnum.TEAM.getValue()) {
                    log.error("图片所在空间不是团队空间，拒绝握手");
                    return false;
                }
            }
            // 改后（直接传入已从 Redis 中取出的 loginUser）
            List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
            if (!permissionList.contains(SpaceUserPermissionConstant.PICTURE_EDIT)) {
                log.error("用户没有编辑图片的权限，拒绝握手");
                return false;
            }

            // 设置属性到 WebSocket Session
            attributes.put("user", loginUser);
            attributes.put("userId", loginUser.getId());
            attributes.put("pictureId", Long.valueOf(pictureId));
        }
        return true;
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler, Exception exception) {
    }
}

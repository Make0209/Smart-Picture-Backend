package com.hbpu.smartpicture.manager.auth;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

/**
 * StpLogic 门面类，管理项目中所有的 StpLogic 账号体系
 * 添加 @Component 注解的目的是确保静态属性 DEFAULT 和 SPACE 被初始化
 */
@Component
public class StpKit {

    public static final String SPACE_TYPE = "space";

    /**
     * 默认原生会话对象
     */
    public static final StpLogic DEFAULT = StpUtil.stpLogic;

    /**
     * Space 会话对象，配置 tokenName 为 space-token，与前端请求头对应
     */
    public static final StpLogic SPACE = new StpLogic(SPACE_TYPE) {
        @Override
        public SaTokenConfig getConfigOrGlobal() {
            SaTokenConfig config = new SaTokenConfig();
            // 指定从请求头 space-token 中读取 token
            config.setTokenName("space-token");
            // 允许从请求头读取（默认 true，显式声明更清晰）
            config.setIsReadHeader(true);
            // 不从 cookie 读取，避免冲突
            config.setIsReadCookie(false);
            return config;
        }
    };
}

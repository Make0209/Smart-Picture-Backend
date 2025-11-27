package com.hbpu.smartpicture.security;

import com.hbpu.smartpicture.exception.BusinessException;
import com.hbpu.smartpicture.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * 生成Token的工具类
 */
public class JwtUtil {
    //指定token加密密钥
    private static final String SECRET_KEY = "HBPU_SMART_PICTURE_SECRET_KEY_FOR_JWT";

    /**
     * 生成Jwt的方法
     *
     * @param userAccount 用户账号
     * @return 返回该用户专属的token
     */
    public static String generateJwt(String userAccount) {
        return Jwts.builder()
                   .subject(userAccount)
                   .issuedAt(new Date())
                   .expiration(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).plusDays(7).toInstant()))
                   .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                   .compact();
    }

    /**
     * 使用key解码token
     *
     * @param token 前端token
     * @return 获取token中的负载
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 判断当前token是否过期
     *
     * @param token 前端传回的当前用户的token
     * @return 已过期返回true，否则返回false
     */
    public static Boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "登录已过期");
        }
    }

    /**
     * 获取当前用户的id
     * @param token 前端传回的当前用户的token
     * @return 当前用户的id
     */
    public static String getUserAccount(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getSubject();
        }  catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "解析token时出现异常！");
        }
    }
}

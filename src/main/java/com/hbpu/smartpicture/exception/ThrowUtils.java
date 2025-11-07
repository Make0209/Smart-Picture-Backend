package com.hbpu.smartpicture.exception;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 异常处理工具类
 */
@Schema(description = "异常处理工具类")
public class ThrowUtils {

    /**
     * 当条件符合时抛出异常
     * @param condition 判断条件
     * @param exception 运行异常
     */
    public static void throwIf(boolean condition, RuntimeException exception) {
        if (condition) {
            throw exception;
        }
    }

    /**
     * 当条件符合时抛出异常
     * @param condition 判断条件
     * @param errorCode 错误码枚举类对象
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }

    /**
     * 当条件符合时抛出自定义错误信息的异常
     * @param condition 判断条件
     * @param errorCode 错误码枚举类对象
     * @param message 自定义错误信息
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new BusinessException(errorCode, message));
    }
}

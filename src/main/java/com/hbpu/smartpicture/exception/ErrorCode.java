package com.hbpu.smartpicture.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 报错响应信息枚举类
 */
@Schema(description = "报错响应信息枚举类")
@Getter
public enum ErrorCode {

    /**
     * 请求成功，状态码为0
     */
    @Schema(hidden = true) SUCCESS(0, "请求成功"),

    /**
     * 请求参数错误，状态码为40000
     */
    @Schema(hidden = true) PARAMS_ERROR(40000, "请求参数错误"),

    /**
     * 未登录错误，状态码为40100
     */
    @Schema(hidden = true) NOT_LOGIN_ERROR(40100, "未登录"),

    /**
     * 无权限错误，状态码为40101
     */
    @Schema(hidden = true) NO_AUTH_ERROR(40101, "无权限"),

    /**
     * 请求数据不存在错误，状态码为40400
     */
    @Schema(hidden = true) NOT_FOUND_ERROR(40400, "请求数据不存在"),

    /**
     * 禁止访问错误，状态码为40300
     */
    @Schema(hidden = true) FORBIDDEN_ERROR(40300, "禁止访问"),

    /**
     * 系统内部异常错误，状态码为50000
     */
    @Schema(hidden = true) SYSTEM_ERROR(50000, "系统内部异常"),

    /**
     * 操作失败错误，状态码为50001
     */
    @Schema(hidden = true) OPERATION_ERROR(50001, "操作失败");

    /**
     * 状态码
     */
    @Schema(description = "状态码")
    private final int code;

    /**
     * 信息
     */
    @Schema(description = "信息")
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}


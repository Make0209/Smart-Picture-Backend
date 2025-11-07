package com.hbpu.smartpicture.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.io.Serial;

/**
 * 自定义业务异常类
 */
@Schema(description = "自定义业务异常类")
@Getter
public class BusinessException extends RuntimeException {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = 1459250089361659848L;
    /**
     * 错误码
     */
    @Schema(description = "错误码")
    private final int code;

    /**
     * 自定义错误码和错误信息
     * @param message 错误信息
     * @param code 错误码
     */
    public BusinessException(String message, int code) {
        super(message);
        this.code = code;
    }

    /**
     * 直接使用错误码枚举类定义错误信息
     * @param errorCode 错误码枚举类对象
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    /**
     * 使用错误码枚举类对象并自定义错误信息
     * @param errorCode 错误码枚举类对象
     * @param message 自定义错误信息
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}

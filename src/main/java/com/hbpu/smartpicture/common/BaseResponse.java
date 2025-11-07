package com.hbpu.smartpicture.common;

import com.hbpu.smartpicture.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 全局响应封装类
 *
 * @param <T> 返回值类型
 */
@Schema(description = "全局响应封装类")
@Data
public class BaseResponse<T> implements Serializable {


    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = -8853237229283271686L;
    /**
     * 状态码
     */
    @Schema(description = "状态码")
    private int code;
    /**
     * 响应信息
     */
    @Schema(description = "响应信息")
    private String message;
    /**
     * 返回体
     */
    @Schema(description = "返回体")
    private T data;

    /**
     * 自定义返回值
     *
     * @param code    状态码
     * @param message 响应信息
     * @param data    返回体
     */
    public BaseResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 返回无响应信息的
     *
     * @param code 状态码
     * @param data 返回体
     */
    public BaseResponse(int code, T data) {
        this(code, "", data);
    }

    /**
     * 返回错误信息
     *
     * @param errorCode 错误码枚举对象
     */
    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMessage(), null);
    }
}

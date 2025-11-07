package com.hbpu.smartpicture.exception;

import com.hbpu.smartpicture.common.BaseResponse;
import com.hbpu.smartpicture.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
//适用于 RESTful 接口项目，它拦截所有controller请求并返回 JSON 格式的错误或结果，而不是网页视图。
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕捉所有项目中的业务异常并返回
     * @param e 业务异常
     * @return 一个包含错误码和错误信息的由全局响应工具类封装的响应封装类对象
     */
    @ExceptionHandler(value = BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException:", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    /**
     * 捕捉所有项目中的运行异常并返回
     * @param e 运行时异常
     * @return 返回系统错误异常
     */
    @ExceptionHandler(value = RuntimeException.class)
    public BaseResponse<?> exceptionHandler(RuntimeException e) {
        log.error("RuntimeException:", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误！");
    }
}

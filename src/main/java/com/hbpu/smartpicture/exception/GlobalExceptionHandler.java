package com.hbpu.smartpicture.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.hbpu.smartpicture.common.BaseResponse;
import com.hbpu.smartpicture.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

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

    /**
     * 捕获前端发送不允许的类型的异常
     * @param ex 不支持的类型的异常
     * @return 返回异常错误信息
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public BaseResponse<?> handleMediaTypeException(HttpMediaTypeNotSupportedException ex) {
        return ResultUtils.error(ErrorCode.FORBIDDEN_ERROR,("不支持的请求类型：" + ex.getContentType()));
    }

    /**
     * 捕获文件按过大的异常
     * @return 返回异常错误信息
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public BaseResponse<?> handleMaxSize() {
        return ResultUtils.error(ErrorCode.OPERATION_ERROR,"上传文件过大，请上传小于限制的文件！");
    }

    /**
     * 捕获用户未登录的异常
     *
     * @param e 未登录的异常
     * @return 返回异常错误信息
     */
    @ExceptionHandler(NotLoginException.class)
    public BaseResponse<?> notLoginException(NotLoginException e) {
        log.error("NotLoginException", e);
        return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR, e.getMessage());
    }

    /**
     * 捕获用户没有权限的异常
     *
     * @param e 没有权限的异常
     * @return 返回异常错误信息
     */
    @ExceptionHandler(NotPermissionException.class)
    public BaseResponse<?> notPermissionExceptionHandler(NotPermissionException e) {
        log.error("NotPermissionException", e);
        return ResultUtils.error(ErrorCode.NO_AUTH_ERROR, e.getMessage());
    }


}

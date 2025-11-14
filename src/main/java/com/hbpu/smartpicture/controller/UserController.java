package com.hbpu.smartpicture.controller;

import com.hbpu.smartpicture.common.BaseResponse;
import com.hbpu.smartpicture.common.ResultUtils;
import com.hbpu.smartpicture.exception.BusinessException;
import com.hbpu.smartpicture.exception.ErrorCode;
import com.hbpu.smartpicture.exception.ThrowUtils;
import com.hbpu.smartpicture.model.dto.UserRegisterDTO;
import com.hbpu.smartpicture.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户功能
 */
@Tag(name = "用户功能", description = "用户功能")
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户注册接口
     *
     * @param userRegisterDTO 用户注册请求参数封装类
     * @return 用户注册成功后的id
     */
    @Operation(summary = "用户注册接口", description = "用户注册接口")
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterDTO userRegisterDTO) {
        ThrowUtils.throwIf(userRegisterDTO == null, new BusinessException(ErrorCode.PARAMS_ERROR));
        return ResultUtils.success(userService.userRegister(userRegisterDTO));
    }
}

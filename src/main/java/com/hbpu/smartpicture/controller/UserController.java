package com.hbpu.smartpicture.controller;

import com.hbpu.smartpicture.annotation.AuthCheck;
import com.hbpu.smartpicture.common.BaseResponse;
import com.hbpu.smartpicture.common.ResultUtils;
import com.hbpu.smartpicture.constant.UserConstant;
import com.hbpu.smartpicture.exception.BusinessException;
import com.hbpu.smartpicture.exception.ErrorCode;
import com.hbpu.smartpicture.exception.ThrowUtils;
import com.hbpu.smartpicture.model.dto.UserLoginDTO;
import com.hbpu.smartpicture.model.dto.UserRegisterDTO;
import com.hbpu.smartpicture.model.pojo.User;
import com.hbpu.smartpicture.model.vo.UserLoginVO;
import com.hbpu.smartpicture.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 用户登录接口
     * @param userLoginDTO 用户登录请求封装类
     * @return 用户登录响应封装类
     */
    @Operation(summary = "用户登录接口", description = "用户登录接口")
    @PostMapping("/login")
    public BaseResponse<UserLoginVO> userLogin(@RequestBody UserLoginDTO userLoginDTO) {
        ThrowUtils.throwIf(userLoginDTO == null, new BusinessException(ErrorCode.PARAMS_ERROR));
        String userAccount = userLoginDTO.getUserAccount();
        String userPassword = userLoginDTO.getUserPassword();
        return ResultUtils.success(userService.userLogin(userAccount, userPassword));
    }

    /**
     * 获取当前登录用户接口
     * @param request 用户请求
     * @return 用户信息封装类
     */
    @Operation(summary = "获取当前登录用户接口", description = "获取当前登录用户接口")
    @GetMapping("/get/login")
    public BaseResponse<UserLoginVO> getLoginUser(HttpServletRequest request) {
        User currentUser = userService.getCurrentUser(request);
        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtils.copyProperties(currentUser, userLoginVO);
        return ResultUtils.success(userLoginVO);
    }

    /**
     * 用户注销接口
     * @param request 用户请求
     * @return 注销成功返回true
     */
    @Operation(summary = "用户注销接口", description = "用户注销接口")
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        return ResultUtils.success(userService.loginOut(request));
    }
}

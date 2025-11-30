package com.hbpu.smartpicture.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbpu.smartpicture.annotation.AuthCheck;
import com.hbpu.smartpicture.common.BaseResponse;
import com.hbpu.smartpicture.common.DeleteRequest;
import com.hbpu.smartpicture.common.ResultUtils;
import com.hbpu.smartpicture.constant.UserConstant;
import com.hbpu.smartpicture.exception.BusinessException;
import com.hbpu.smartpicture.exception.ErrorCode;
import com.hbpu.smartpicture.exception.ThrowUtils;
import com.hbpu.smartpicture.model.dto.user.*;
import com.hbpu.smartpicture.model.pojo.User;
import com.hbpu.smartpicture.model.vo.user.UserLoginVO;
import com.hbpu.smartpicture.model.vo.user.UserVO;
import com.hbpu.smartpicture.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     *
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
     *
     * @param request 用户请求
     * @return 用户信息封装类
     */
    @Operation(summary = "获取当前登录用户接口", description = "获取当前登录用户接口")
    @GetMapping("/get/login")
    @AuthCheck(mustRole = UserConstant.ROLE_USER)
    public BaseResponse<UserVO> getLoginUser(HttpServletRequest request) {
        UserVO userVO = userService.getUserVO(userService.getCurrentUser(request));
        return ResultUtils.success(userVO);
    }

    /**
     * 用户注销接口
     *
     * @param request 用户请求
     * @return 注销成功返回true
     */
    @Operation(summary = "用户注销接口", description = "用户注销接口")
    @PostMapping("/logout")
    @AuthCheck(mustRole = UserConstant.ROLE_USER)
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        return ResultUtils.success(userService.loginOut(request));
    }

    /**
     * 【管理员】添加用户
     *
     * @param userAddDTO 接收用户参数的封装类
     * @return 创建成功后的用户id
     */
    @Operation(summary = "【管理员】添加用户", description = "【管理员】添加用户")
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ROLE_ADMIN)
    public BaseResponse<Long> userAdd(@RequestBody UserAddDTO userAddDTO) {
        ThrowUtils.throwIf(
                userAddDTO == null,
                new BusinessException(ErrorCode.PARAMS_ERROR)
        );
        User user = new User();
        BeanUtils.copyProperties(userAddDTO, user);
        final String DEFAULT_USER_PASSWORD = "12345678";
        user.setUserPassword(userService.getEncryptedPassword(DEFAULT_USER_PASSWORD));
        try {
            boolean saved = userService.save(user);
            ThrowUtils.throwIf(!saved,
                               new BusinessException(ErrorCode.OPERATION_ERROR, "插入数据时出现错误！"));
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在！");
        }

        return ResultUtils.success(user.getId());
    }

    /**
     * 【管理员】根据id查询用户
     *
     * @param id 用户id
     * @return User对象
     */
    @Parameter(name = "id", description = "用户id", in = ParameterIn.PATH, required = true)
    @Operation(summary = "【管理员】根据id查询用户", description = "【管理员】根据id查询用户")
    @GetMapping("/get/{id}")
    @AuthCheck(mustRole = UserConstant.ROLE_ADMIN)
    public BaseResponse<User> getUserByID(@PathVariable Long id) {
        ThrowUtils.throwIf(id < 0, new BusinessException(ErrorCode.PARAMS_ERROR));
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, new BusinessException(ErrorCode.NOT_FOUND_ERROR));
        return ResultUtils.success(user);
    }

    /**
     * 根据id查询用户
     *
     * @param id 用户id
     * @return 脱敏后的用户信息封装类
     */
    @Parameter(name = "id", description = "用户id", in = ParameterIn.PATH, required = true)
    @Operation(summary = "根据id查询用户", description = "根据id查询用户")
    @GetMapping("/get/vo/{id}")
    @AuthCheck(mustRole = UserConstant.ROLE_USER)
    public BaseResponse<UserVO> getUserVOByID(@PathVariable Long id) {
        ThrowUtils.throwIf(id < 0, new BusinessException(ErrorCode.PARAMS_ERROR));
        UserVO userVO = userService.getUserVO(this.getUserByID(id).getData());
        return ResultUtils.success(userVO);
    }

    /**
     * 【管理员】根据id删除用户
     *
     * @param deleteRequest 接收删除用户请求的封装类
     * @return 删除成功返回true
     */
    @Operation(summary = "【管理员】根据id删除用户", description = "【管理员】根据id删除用户")
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ROLE_ADMIN)
    public BaseResponse<Boolean> userDelete(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(
                deleteRequest == null || deleteRequest.getId() < 0,
                new BusinessException(ErrorCode.PARAMS_ERROR)
        );
        ThrowUtils.throwIf(
                !userService.removeById(deleteRequest.getId()),
                new BusinessException(ErrorCode.OPERATION_ERROR, "你要删除的id错误或这个不存在！")
        );
        return ResultUtils.success(true);
    }

    /**
     * 更新用户
     *
     * @param userUpdateDTO 接收更新用户信息的封装类
     * @return 更新成功返回true
     */
    @Operation(summary = "更新用户", description = "更新用户")
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ROLE_USER)
    public BaseResponse<Boolean> userUpdate(@RequestBody UserUpdateDTO userUpdateDTO) {
        ThrowUtils.throwIf(
                userUpdateDTO == null,
                new BusinessException(ErrorCode.PARAMS_ERROR)
        );
        User user = new User();
        BeanUtils.copyProperties(userUpdateDTO, user);
        ;
        ThrowUtils.throwIf(
                !userService.updateById(user),
                new BusinessException(ErrorCode.OPERATION_ERROR, "更新用户失败！")
        );
        return ResultUtils.success(true);
    }

    /**
     * 【管理员】根据条件查询
     *
     * @param userQueryDTO 接收分页条件和查询条件的封装类
     * @return 返回每页的Page对象
     */
    @Operation(summary = "【管理员】根据条件查询", description = "【管理员】根据条件查询")
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ROLE_ADMIN)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryDTO userQueryDTO) {
        ThrowUtils.throwIf(
                userQueryDTO == null,
                new BusinessException(ErrorCode.PARAMS_ERROR)
        );

        int current = userQueryDTO.getCurrent();
        int pageSize = userQueryDTO.getPageSize();
        Page<User> page;
        Page<UserVO> userVOPage;
        try {
            //现根据查询条件和分页条件查出数据
            page = userService.page(new Page<>(current, pageSize), userService.getQueryWrapper(userQueryDTO));
            //根据查询出来的数据获取总条数，
            // 然后结合当前位置和页面大小创建一个新的Page对象，
            // 用来容纳脱敏后的对象，需要总数是因为前端页面控件需要总数，不然无法正常显示
            userVOPage = new Page<>(current, pageSize, page.getTotal());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "分页查询时出现错误！");
        }
        //脱敏数据并将其加入到新Page对象中返回给前端
        List<UserVO> userVOList = userService.getUserVOList(page.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }
}

package com.hbpu.smartpicture.controller;

import cn.hutool.core.util.ObjectUtil;
import com.hbpu.smartpicture.annotation.AuthCheck;
import com.hbpu.smartpicture.common.BaseResponse;
import com.hbpu.smartpicture.common.DeleteRequest;
import com.hbpu.smartpicture.common.ResultUtils;
import com.hbpu.smartpicture.constant.UserConstant;
import com.hbpu.smartpicture.exception.BusinessException;
import com.hbpu.smartpicture.exception.ErrorCode;
import com.hbpu.smartpicture.exception.ThrowUtils;
import com.hbpu.smartpicture.manager.auth.annotation.SaSpaceCheckPermission;
import com.hbpu.smartpicture.manager.auth.model.SpaceUserPermissionConstant;
import com.hbpu.smartpicture.model.dto.spaceuser.SpaceUserAddDTO;
import com.hbpu.smartpicture.model.dto.spaceuser.SpaceUserQueryDTO;
import com.hbpu.smartpicture.model.pojo.SpaceUser;
import com.hbpu.smartpicture.model.pojo.User;
import com.hbpu.smartpicture.model.vo.spaceuser.SpaceUserVO;
import com.hbpu.smartpicture.service.SpaceUserService;
import com.hbpu.smartpicture.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 团队空间功能控制器
 *
 * @author 马可
 */
@Tag(name = "SpaceUserController", description = "团队空间功能控制器")
@RestController
@RequestMapping("/teamSpace")
@Slf4j
public class SpaceUserController {

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private UserService userService;

    /**
     * 创建一个团队空间
     *
     * @param spaceUserAddDTO 创建团队空间的参数
     * @return 创建的团队空间的 ID
     */
    @Operation(summary = "创建一个团队空间", description = "创建一个团队空间")
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ROLE_USER)
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Long> addSpaceUser(@RequestBody SpaceUserAddDTO spaceUserAddDTO) {
        ThrowUtils.throwIf(spaceUserAddDTO == null, ErrorCode.PARAMS_ERROR);
        long id = spaceUserService.addSpaceUser(spaceUserAddDTO);
        return ResultUtils.success(id);
    }

    /**
     * 删除团队空间中的成员
     *
     * @param deleteRequest 删除团队空间成员的参数
     * @return 删除是否成功
     */
    @Operation(summary = "删除团队空间中的成员", description = "删除团队空间中的成员")
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ROLE_USER)
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> deleteSpaceUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 这里传的id不是用户id，而是用户加入整个团队空间数据记录的id，没有了这条记录，相当于删除了团队空间中的成员
        long id = deleteRequest.getId();
        // 判断是否存在
        SpaceUser oldSpaceUser = spaceUserService.getById(id);
        ThrowUtils.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = spaceUserService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 获取团队空间成员信息
     *
     * @param spaceUserQueryDTO 获取团队空间成员信息的参数
     * @return 团队空间成员信息
     */
    @Operation(summary = "获取团队空间成员信息", description = "获取团队空间成员信息")
    @PostMapping("/get")
    @AuthCheck(mustRole = UserConstant.ROLE_USER)
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<SpaceUser> getSpaceUser(@RequestBody SpaceUserQueryDTO spaceUserQueryDTO) {
        // 参数校验
        ThrowUtils.throwIf(spaceUserQueryDTO == null, ErrorCode.PARAMS_ERROR);
        Long spaceId = spaceUserQueryDTO.getSpaceId();
        Long userId = spaceUserQueryDTO.getUserId();
        ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId, userId), ErrorCode.PARAMS_ERROR);
        // 查询数据库
        SpaceUser spaceUser = spaceUserService.getOne(spaceUserService.getQueryWrapper(spaceUserQueryDTO));
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(spaceUser);
    }

    /**
     * 获取团队空间成员信息列表
     *
     * @param spaceUserQueryDTO 获取团队空间成员信息列表的参数
     * @return 团队空间成员信息列表
     */
    @Operation(summary = "获取团队空间成员信息列表", description = "获取团队空间成员信息列表")
    @PostMapping("/list")
    @AuthCheck(mustRole = UserConstant.ROLE_USER)
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<List<SpaceUserVO>> listSpaceUser(@RequestBody SpaceUserQueryDTO spaceUserQueryDTO) {
        ThrowUtils.throwIf(spaceUserQueryDTO == null, ErrorCode.PARAMS_ERROR);
        List<SpaceUser> spaceUserList = spaceUserService.list(
                spaceUserService.getQueryWrapper(spaceUserQueryDTO)
        );
        return ResultUtils.success(spaceUserService.getSpaceUserVOList(spaceUserList));
    }

    /**
     * 编辑成员信息（设置权限）
     *
     * @param spaceUserQueryDTO 编辑成员信息（设置权限）的参数
     * @return 编辑是否成功
     */
    @Operation(summary = "编辑成员信息（设置权限）", description = "编辑成员信息（设置权限）")
    @PostMapping("/edit")
    @AuthCheck(mustRole = UserConstant.ROLE_USER)
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> editSpaceUser(@RequestBody SpaceUserQueryDTO spaceUserQueryDTO) {
        if (spaceUserQueryDTO == null || spaceUserQueryDTO.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将实体类和 DTO 进行转换
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserQueryDTO, spaceUser);
        // 数据校验
        spaceUserService.validSpaceUser(spaceUser, false);
        // 判断是否存在
        long id = spaceUserQueryDTO.getId();
        SpaceUser oldSpaceUser = spaceUserService.getById(id);
        ThrowUtils.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = spaceUserService.updateById(spaceUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 查询我加入的团队空间列表
     *
     * @param request 登录用户信息，用于获取当前登录用户
     * @return 我加入的团队空间列表
     */
    @Operation(summary = "查询我加入的团队空间列表", description = "查询我加入的团队空间列表")
    @PostMapping("/list/my")
    @AuthCheck(mustRole = UserConstant.ROLE_USER)
    public BaseResponse<List<SpaceUserVO>> listMyTeamSpace(HttpServletRequest request) {
        User loginUser = userService.getCurrentUser(request);
        SpaceUserQueryDTO spaceUserQueryDTO = new SpaceUserQueryDTO();
        spaceUserQueryDTO.setUserId(loginUser.getId());
        List<SpaceUser> spaceUserList = spaceUserService.list(
                spaceUserService.getQueryWrapper(spaceUserQueryDTO)
        );
        return ResultUtils.success(spaceUserService.getSpaceUserVOList(spaceUserList));
    }
}


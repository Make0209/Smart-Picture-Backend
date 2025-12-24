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
import com.hbpu.smartpicture.model.dto.space.SpaceAddDTO;
import com.hbpu.smartpicture.model.dto.space.SpaceEditDTO;
import com.hbpu.smartpicture.model.dto.space.SpaceQueryDTO;
import com.hbpu.smartpicture.model.dto.space.SpaceUpdateDTO;
import com.hbpu.smartpicture.model.pojo.Space;
import com.hbpu.smartpicture.model.pojo.User;
import com.hbpu.smartpicture.model.vo.space.SpaceVO;
import com.hbpu.smartpicture.service.SpaceService;
import com.hbpu.smartpicture.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 空间功能接口
 */
@Tag(name = "空间功能接口", description = "空间功能接口")
@RestController
@RequestMapping("/space")
@Slf4j
public class SpaceController {
    private final SpaceService spaceService;
    private final UserService userService;

    public SpaceController(SpaceService spaceService, UserService userService) {
        this.spaceService = spaceService;
        this.userService = userService;
    }

    /**
     * 创建空间接口
     * @param spaceAddDTO 创建空间请求信息封装类
     * @param request 用户请求
     * @return 返回创建成功后的SpaceID
     */
    @Operation(summary = "创建空间接口", description = "创建空间接口")
    @AuthCheck(mustRole = UserConstant.ROLE_USER)
    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddDTO  spaceAddDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceAddDTO == null, ErrorCode.PARAMS_ERROR);
        Long result = spaceService.addSpace(spaceAddDTO, request);
        return ResultUtils.success(result);
    }

    /**
     * 删除空间
     *
     * @param deleteRequest 删除请求封装类
     * @param request       用户请求
     * @return 删除成功返回true
     */
    @Operation(summary = "删除空间", description = "删除空间")
    @AuthCheck(mustRole = UserConstant.ROLE_USER)
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User currentUser = userService.getCurrentUser(request);
        Space space = spaceService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR, "要删除的空间不存在！");
        ThrowUtils.throwIf(
                !Objects.equals(space.getUserId(), currentUser.getId()) &&
                        !Objects.equals(currentUser.getUserRole(), "admin"),
                ErrorCode.PARAMS_ERROR,
                "没有权限删除该空间！"
        );
        ThrowUtils.throwIf(!spaceService.removeById(deleteRequest.getId()), ErrorCode.OPERATION_ERROR, "删除失败！");
        return ResultUtils.success(true);
    }

    /**
     * 【管理员】更新空间信息
     * @param spaceUpdateDTO 更新空间请求封装类
     * @return 更新成功返回true
     */
    @Operation(summary = "【管理员】更新空间信息", description = "【管理员】更新空间信息")
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ROLE_ADMIN)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateDTO spaceUpdateDTO) {
        ThrowUtils.throwIf(spaceUpdateDTO == null || spaceUpdateDTO.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 将实体类和 DTO 进行转换
        Space space = new Space();
        BeanUtils.copyProperties(spaceUpdateDTO, space);
        // 自动填充数据
        spaceService.fillSpaceBySpaceLevel(space);
        // 数据校验
        spaceService.validSpace(space, false);
        // 判断是否存在
        long id = spaceUpdateDTO.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 【管理员】根据id获取空间对象
     *
     * @param id 空间id
     * @return 返回空间对象
     */
    @Operation(summary = "【管理员】根据id获取空间对象", description = "【管理员】根据id获取空间对象")
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ROLE_ADMIN)
    public BaseResponse<Space> getSpaceById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(space);
    }

    /**
     * 根据空间id获取VO对象
     *
     * @param id      空间id
     * @param request 用户请求
     * @return 返回SpaceVO对象
     */
    @Operation(summary = "根据空间id获取VO对象", description = "根据空间id获取VO对象")
    @AuthCheck(mustRole = UserConstant.ROLE_USER)
    @GetMapping("/get/vo")
    public BaseResponse<SpaceVO> getSpaceVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(spaceService.getSpaceVO(space, request));
    }

    /**
     * 【管理员】分页获取空间信息
     *
     * @param spaceQueryDTO 分页空间信息封装类
     * @return 返回空间分页对象封装类
     */
    @Operation(summary = "【管理员】分页获取空间信息", description = "【管理员】分页获取空间信息")
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ROLE_ADMIN)
    public BaseResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryDTO spaceQueryDTO) {
        long current = spaceQueryDTO.getCurrent();
        long size = spaceQueryDTO.getPageSize();
        // 查询数据库
        Page<Space> spacePage = spaceService.page(
                new Page<>(current, size),
                spaceService.getQueryWrapper(spaceQueryDTO)
        );
        return ResultUtils.success(spacePage);
    }

    /**
     * 分页获取SpaceVO对象
     *
     * @param spaceQueryDTO 分页空间信息封装类
     * @return 分页SpaceVO对象封装类
     */
    @Operation(summary = "分页获取SpaceVO对象", description = "分页获取SpaceVO对象")
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryDTO spaceQueryDTO) {
        long current = spaceQueryDTO.getCurrent();
        long size = spaceQueryDTO.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Space> spacePage = spaceService.page(
                new Page<>(current, size),
                spaceService.getQueryWrapper(spaceQueryDTO)
        );
        // 获取封装类
        return ResultUtils.success(spaceService.getSpaceVOPage(spacePage));
    }

    /**
     * 编辑空间
     *
     * @param spaceEditDTO 空间编辑信息封装类
     * @param request        用户请求
     * @return 编辑成功返回true
     */
    @Operation(summary = "编辑空间", description = "编辑空间")
    @AuthCheck(mustRole = UserConstant.ROLE_USER)
    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditDTO spaceEditDTO, HttpServletRequest request) {
        if (spaceEditDTO == null || spaceEditDTO.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 在此处将实体类和 DTO 进行转换
        Space space = new Space();
        BeanUtils.copyProperties(spaceEditDTO, space);
        // 自动填充数据
        spaceService.fillSpaceBySpaceLevel(space);
        // 数据校验
        spaceService.validSpace(space, false);
        User loginUser = userService.getCurrentUser(request);
        // 判断是否存在
        Space oldSpace = spaceService.getById(spaceEditDTO.getId());
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        ThrowUtils.throwIf(
                !oldSpace.getUserId().equals(loginUser.getId()) &&
                        !Objects.equals(loginUser.getUserRole(), "admin"),
                ErrorCode.NO_AUTH_ERROR
        );
        // 操作数据库
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

}

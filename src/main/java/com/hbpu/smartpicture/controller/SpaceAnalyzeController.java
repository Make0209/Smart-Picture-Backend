package com.hbpu.smartpicture.controller;

import com.hbpu.smartpicture.annotation.AuthCheck;
import com.hbpu.smartpicture.common.BaseResponse;
import com.hbpu.smartpicture.common.ResultUtils;
import com.hbpu.smartpicture.constant.UserConstant;
import com.hbpu.smartpicture.exception.ErrorCode;
import com.hbpu.smartpicture.exception.ThrowUtils;
import com.hbpu.smartpicture.model.dto.space.analyze.*;
import com.hbpu.smartpicture.model.vo.space.analyze.*;
import com.hbpu.smartpicture.service.SpaceAnalyzeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 空间分析控制器
 *
 **/
@Tag(name = "SpaceAnalyzeController*/", description = "空间分析控制器*/")
@RestController
@RequestMapping("/analyze")
@Slf4j
public class SpaceAnalyzeController {

    private final SpaceAnalyzeService spaceAnalyzeService;

    public SpaceAnalyzeController(SpaceAnalyzeService spaceAnalyzeService) {
        this.spaceAnalyzeService = spaceAnalyzeService;
    }

    /**
     * 获取空间使用分析
     *
     * @param spaceUsageAnalyzeDTO 空间使用分析参数
     * @param request              用户请求
     * @return 空间使用分析结果
     */
    @Operation(summary = "获取空间使用分析", description = "获取空间使用分析")
    @PostMapping("/usage")
    @AuthCheck(mustRole = UserConstant.ROLE_USER)
    public BaseResponse<SpaceUsageAnalyzeVO> getSpaceUsageAnalyze(@RequestBody SpaceUsageAnalyzeDTO spaceUsageAnalyzeDTO, HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(spaceUsageAnalyzeDTO == null, ErrorCode.PARAMS_ERROR);
        SpaceUsageAnalyzeVO spaceUsageAnalyze = spaceAnalyzeService.getSpaceUsageAnalyze(spaceUsageAnalyzeDTO, request);
        return ResultUtils.success(spaceUsageAnalyze);
    }

    /**
     * 获取空间图片分类分析
     *
     * @param spaceCategoryAnalyzeDTO 空间图片分类分析参数
     * @param request                 用户请求
     * @return 空间图片分类分析结果
     */
    @Operation(summary = "获取空间图片分类分析", description = "获取空间图片分类分析")
    @PostMapping("/category")
    @AuthCheck(mustRole = UserConstant.ROLE_USER)
    public BaseResponse<List<SpaceCategoryAnalyzeVO>> getSpaceCategoryAnalyze(@RequestBody SpaceCategoryAnalyzeDTO spaceCategoryAnalyzeDTO, HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(spaceCategoryAnalyzeDTO == null, ErrorCode.PARAMS_ERROR);
        List<SpaceCategoryAnalyzeVO> spaceCategoryAnalyze = spaceAnalyzeService.getSpaceCategoryAnalyze(
                spaceCategoryAnalyzeDTO, request);
        return ResultUtils.success(spaceCategoryAnalyze);
    }

    /**
     * 获取空间图片标签分析
     *
     * @param spaceTagAnalyzeDTO 空间图片标签分析参数
     * @param request            用户请求
     * @return 空间图片标签分析结果
     */
    @Operation(summary = "获取空间图片标签分析", description = "获取空间图片标签分析")
    @PostMapping("/tag")
    @AuthCheck(mustRole = UserConstant.ROLE_USER)
    public BaseResponse<List<SpaceTagAnalyzeVO>> getSpaceTagAnalyze(@RequestBody SpaceTagAnalyzeDTO spaceTagAnalyzeDTO, HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(spaceTagAnalyzeDTO == null, ErrorCode.PARAMS_ERROR);
        // 调用服务层获取空间标签分析结果
        List<SpaceTagAnalyzeVO> spaceTagAnalyze = spaceAnalyzeService.getSpaceTagAnalyze(spaceTagAnalyzeDTO, request);
        return ResultUtils.success(spaceTagAnalyze);
    }

    /**
     * 获取空间图片大小分析
     *
     * @param spaceSizeAnalyzeDTO 空间图片大小分析参数
     * @param request             用户请求
     * @return 空间图片大小分析结果
     */
    @Operation(summary = "获取空间图片大小分析", description = "获取空间图片大小分析")
    @PostMapping("/size")
    @AuthCheck(mustRole = UserConstant.ROLE_USER)
    public BaseResponse<List<SpaceSizeAnalyzeVO>> getSpaceSizeAnalyze(@RequestBody SpaceSizeAnalyzeDTO spaceSizeAnalyzeDTO, HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(spaceSizeAnalyzeDTO == null, ErrorCode.PARAMS_ERROR);
        // 调用服务层获取空间图片大小分析结果
        List<SpaceSizeAnalyzeVO> spaceSizeAnalyze = spaceAnalyzeService.getSpaceSizeAnalyze(
                spaceSizeAnalyzeDTO, request);
        return ResultUtils.success(spaceSizeAnalyze);
    }

    /**
     * 获取空间用户上传行为分析
     *
     * @param spaceUserAnalyzeDTO 空间用户上传行为分析参数
     * @param request             用户请求
     * @return 空间用户分析结果
     */
    @Operation(summary = "获取空间用户上传行为分析", description = "获取空间用户上传行为分析")
    @PostMapping("/user")
    @AuthCheck(mustRole = UserConstant.ROLE_USER)
    public BaseResponse<List<SpaceUserAnalyzeVO>> getSpaceUserAnalyze(@RequestBody SpaceUserAnalyzeDTO spaceUserAnalyzeDTO, HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(spaceUserAnalyzeDTO == null, ErrorCode.PARAMS_ERROR);
        // 调用服务层获取空间用户分析结果
        List<SpaceUserAnalyzeVO> spaceUserAnalyze = spaceAnalyzeService.getSpaceUserAnalyze(
                spaceUserAnalyzeDTO, request);
        return ResultUtils.success(spaceUserAnalyze);
    }

}

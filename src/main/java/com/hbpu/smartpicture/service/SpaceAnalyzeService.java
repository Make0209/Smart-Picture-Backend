package com.hbpu.smartpicture.service;

import com.hbpu.smartpicture.model.dto.space.analyze.*;
import com.hbpu.smartpicture.model.vo.space.analyze.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 空间分析服务接口
 */

public interface SpaceAnalyzeService {
    /**
     * 获取空间使用分析参数
     *
     * @param spaceUsageAnalyzeDTO 空间使用分析参数封装类
     * @param request              HTTP请求
     * @return 空间使用分析参数响应封装类
     */
    SpaceUsageAnalyzeVO getSpaceUsageAnalyze(SpaceUsageAnalyzeDTO spaceUsageAnalyzeDTO, HttpServletRequest request);

    /**
     * 获取空间图片分类分析参数
     *
     * @param spaceCategoryAnalyzeDTO 空间图片分类分析参数封装类
     * @param request                 HTTP请求
     * @return 空间图片分类分析参数响应封装类
     */
    List<SpaceCategoryAnalyzeVO> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeDTO spaceCategoryAnalyzeDTO, HttpServletRequest request);

    /**
     * 获取空间图片标签分析参数
     *
     * @param spaceTagAnalyzeDTO 空间图片标签分析参数封装类
     * @param request            HTTP请求
     * @return 空间图片标签分析参数响应封装类
     */
    List<SpaceTagAnalyzeVO> getSpaceTagAnalyze(SpaceTagAnalyzeDTO spaceTagAnalyzeDTO, HttpServletRequest request);

    /**
     * 获取空间图片大小分析参数
     *
     * @param spaceSizeAnalyzeDTO 空间图片大小分析参数封装类
     * @param request             HTTP请求
     * @return 空间图片大小分析参数响应封装类
     */
    List<SpaceSizeAnalyzeVO> getSpaceSizeAnalyze(SpaceSizeAnalyzeDTO spaceSizeAnalyzeDTO, HttpServletRequest request);

    /**
     * 获取空间用户上传行为分析参数
     *
     * @param spaceUserAnalyzeDTO 空间用户上传行为分析参数封装类
     * @param request             HTTP请求
     * @return 空间用户上传行为分析参数响应封装类
     */
    List<SpaceUserAnalyzeVO> getSpaceUserAnalyze(SpaceUserAnalyzeDTO spaceUserAnalyzeDTO, HttpServletRequest request);
}

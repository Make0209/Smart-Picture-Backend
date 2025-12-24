package com.hbpu.smartpicture.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbpu.smartpicture.model.dto.space.SpaceAddDTO;
import com.hbpu.smartpicture.model.dto.space.SpaceQueryDTO;
import com.hbpu.smartpicture.model.pojo.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hbpu.smartpicture.model.vo.space.SpaceVO;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author 马可
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-12-24 20:43:25
*/
public interface SpaceService extends IService<Space> {

    /**
     * 检验Space信息
     * @param space 目标信息
     * @param add 是否为添加请求
     */
    void validSpace(Space space, boolean add);

    /**
     * 获取空间信息封装类
     *
     * @param space 目标对象
     * @param request 用户请求
     * @return 返回VO对象
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 获取空间信息封装类（分页）
     *
     * @param spacePage 图片分页对象
     * @return 返回SpaceVO的分页封装类
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage);

    /**
     * 根据查询条件封装类生成查询条件
     *
     * @param spaceQueryDTO 查询条件封装类
     * @return 返回构建好的查询语句
     */
    LambdaQueryWrapper<Space> getQueryWrapper(SpaceQueryDTO spaceQueryDTO);

    /**
     * 根据目标对象的空间级别自动填充最大空间容量和数量
     * @param space 目标Space对象
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 创建空间
     * @param spaceAddDTO 创建空间请求封装类
     * @param request 用户请求
     * @return 创建成功后的空间的id
     */
    Long addSpace(SpaceAddDTO spaceAddDTO, HttpServletRequest request);
}

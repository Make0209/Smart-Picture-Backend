package com.hbpu.smartpicture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hbpu.smartpicture.model.dto.spaceuser.SpaceUserAddDTO;
import com.hbpu.smartpicture.model.dto.spaceuser.SpaceUserQueryDTO;
import com.hbpu.smartpicture.model.pojo.SpaceUser;
import com.hbpu.smartpicture.model.vo.spaceuser.SpaceUserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * @author 马可
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service
 * @createDate 2026-02-13 17:58:30
 */
public interface SpaceUserService extends IService<SpaceUser> {

    /**
     * 添加空间用户
     *
     * @param spaceUserAddDTO 添加空间用户请求参数封装类
     * @return 返回添加成功后的空间用户ID
     */
    long addSpaceUser(SpaceUserAddDTO spaceUserAddDTO);

    /**
     * 验证空间用户
     *
     * @param spaceUser 空间用户对象
     * @param add       是否添加
     */
    void validSpaceUser(SpaceUser spaceUser, boolean add);

    /**
     * 获取查询条件
     *
     * @param spaceUserQueryDTO 空间用户查询条件封装类
     * @return 查询条件
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryDTO spaceUserQueryDTO);

    /**
     * 获取空间用户VO
     *
     * @param spaceUser 空间用户对象
     * @param request   HTTP请求对象
     * @return 空间用户VO对象
     */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    /**
     * 获取空间用户VO列表
     *
     * @param spaceUserList 空间用户对象列表
     * @return 空间用户VO对象列表
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);
}

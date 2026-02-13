package com.hbpu.smartpicture.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbpu.smartpicture.exception.BusinessException;
import com.hbpu.smartpicture.exception.ErrorCode;
import com.hbpu.smartpicture.exception.ThrowUtils;
import com.hbpu.smartpicture.mapper.SpaceUserMapper;
import com.hbpu.smartpicture.model.dto.spaceuser.SpaceUserAddDTO;
import com.hbpu.smartpicture.model.dto.spaceuser.SpaceUserQueryDTO;
import com.hbpu.smartpicture.model.enums.SpaceRoleEnum;
import com.hbpu.smartpicture.model.pojo.Space;
import com.hbpu.smartpicture.model.pojo.SpaceUser;
import com.hbpu.smartpicture.model.pojo.User;
import com.hbpu.smartpicture.model.vo.space.SpaceVO;
import com.hbpu.smartpicture.model.vo.spaceuser.SpaceUserVO;
import com.hbpu.smartpicture.model.vo.user.UserVO;
import com.hbpu.smartpicture.service.SpaceService;
import com.hbpu.smartpicture.service.SpaceUserService;
import com.hbpu.smartpicture.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 马可
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
 * @createDate 2026-02-13 17:58:30
 */
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
        implements SpaceUserService {

    private final SpaceService spaceService;
    private final UserService userService;

    public SpaceUserServiceImpl(SpaceService spaceService, UserService userService) {
        this.spaceService = spaceService;
        this.userService = userService;
    }

    /**
     * 添加团队空间用户
     *
     * @param spaceUserAddDTO 添加团队空间用户参数
     * @return 返回添加成功后的团队空间的id
     */
    @Override
    public long addSpaceUser(SpaceUserAddDTO spaceUserAddDTO) {
        // 参数校验
        ThrowUtils.throwIf(spaceUserAddDTO == null, ErrorCode.PARAMS_ERROR);
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserAddDTO, spaceUser);
        validSpaceUser(spaceUser, true);
        // 数据库操作
        boolean result = this.save(spaceUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return spaceUser.getId();
    }

    /**
     * 校验空间用户
     *
     * @param spaceUser 空间用户
     * @param add       是否添加
     */
    @Override
    public void validSpaceUser(SpaceUser spaceUser, boolean add) {
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.PARAMS_ERROR);
        // 创建时，空间 id 和用户 id 必填
        Long spaceId = spaceUser.getSpaceId();
        Long userId = spaceUser.getUserId();
        if (add) {
            ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId, userId), ErrorCode.PARAMS_ERROR);
            User user = userService.getById(userId);
            ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        }
        // 校验空间角色
        String spaceRole = spaceUser.getSpaceRole();
        SpaceRoleEnum spaceRoleEnum = SpaceRoleEnum.getEnumByValue(spaceRole);
        if (spaceRole != null && spaceRoleEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间角色不存在");
        }
    }

    /**
     * 获取查询条件
     *
     * @param spaceUserQueryDTO 空间用户查询参数
     * @return 查询条件
     */
    @Override
    public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryDTO spaceUserQueryDTO) {
        QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
        if (spaceUserQueryDTO == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = spaceUserQueryDTO.getId();
        Long spaceId = spaceUserQueryDTO.getSpaceId();
        Long userId = spaceUserQueryDTO.getUserId();
        String spaceRole = spaceUserQueryDTO.getSpaceRole();
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceRole), "spaceRole", spaceRole);
        return queryWrapper;
    }

    /**
     * 获取空间用户封装类
     *
     * @param spaceUser 空间用户
     * @param request   请求
     * @return 空间用户封装类
     */
    @Override
    public SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request) {
        // 对象转封装类
        SpaceUserVO spaceUserVO = SpaceUserVO.objToVo(spaceUser);
        // 关联查询用户信息
        Long userId = spaceUser.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceUserVO.setUser(userVO);
        }
        // 关联查询空间信息
        Long spaceId = spaceUser.getSpaceId();
        if (spaceId != null && spaceId > 0) {
            Space space = spaceService.getById(spaceId);
            SpaceVO spaceVO = spaceService.getSpaceVO(space, request);
            spaceUserVO.setSpace(spaceVO);
        }
        return spaceUserVO;
    }

    /**
     * 获取团队空间用户信息封装类列表
     *
     * @param spaceUserList 空间用户列表
     * @return 团队空间用户信息封装类列表
     */
    @Override
    public List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList) {
        // 判断输入列表是否为空
        if (CollUtil.isEmpty(spaceUserList)) {
            return Collections.emptyList();
        }
        // 对象列表 => 封装对象列表
        List<SpaceUserVO> spaceUserVOList = spaceUserList.stream().map(SpaceUserVO::objToVo).collect(
                Collectors.toList());
        // 1. 收集需要关联查询的用户 ID 和空间 ID
        Set<Long> userIdSet = spaceUserList.stream().map(SpaceUser::getUserId).collect(Collectors.toSet());
        Set<Long> spaceIdSet = spaceUserList.stream().map(SpaceUser::getSpaceId).collect(Collectors.toSet());
        // 2. 批量查询用户和空间
        // 批量查询用户信息，并构建 userId -> User 的映射关系
        // 使用 toMap 将查询结果按照用户ID映射，方便后续快速查找
        Map<Long, User> userIdUserMap = userService.listByIds(userIdSet).stream()
                                                   .collect(Collectors.toMap(User::getId, user -> user));
        // 批量查询空间信息，并构建 spaceId -> Space 的映射关系
        // 使用 toMap 将查询结果按照空间ID映射，方便后续快速查找
        // 这种批量查询的方式避免了在循环中逐个查询，大大提高了性能（N+1问题的解决方案）
        Map<Long, Space> spaceIdSpaceMap = spaceService.listByIds(spaceIdSet).stream()
                                                       .collect(Collectors.toMap(Space::getId, space -> space));
        // 3. 填充 SpaceUserVO 的用户和空间信息
        spaceUserVOList.forEach(spaceUserVO -> {
            Long userId = spaceUserVO.getUserId();
            Long spaceId = spaceUserVO.getSpaceId();
            // 填充用户信息
            User user = null;
            if (userIdUserMap.containsKey(userId)) {
                user = userIdUserMap.get(userId);
            }
            spaceUserVO.setUser(userService.getUserVO(user));
            // 填充空间信息
            Space space = null;
            if (spaceIdSpaceMap.containsKey(spaceId)) {
                space = spaceIdSpaceMap.get(spaceId);
            }
            spaceUserVO.setSpace(SpaceVO.objToVo(space));
        });
        return spaceUserVOList;
    }

}





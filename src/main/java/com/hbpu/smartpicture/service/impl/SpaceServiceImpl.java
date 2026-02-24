package com.hbpu.smartpicture.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbpu.smartpicture.exception.ErrorCode;
import com.hbpu.smartpicture.exception.ThrowUtils;
import com.hbpu.smartpicture.mapper.SpaceMapper;
import com.hbpu.smartpicture.model.dto.space.SpaceAddDTO;
import com.hbpu.smartpicture.model.dto.space.SpaceQueryDTO;
import com.hbpu.smartpicture.model.enums.SpaceLevelEnum;
import com.hbpu.smartpicture.model.enums.SpaceRoleEnum;
import com.hbpu.smartpicture.model.enums.SpaceTypeEnum;
import com.hbpu.smartpicture.model.pojo.Space;
import com.hbpu.smartpicture.model.pojo.SpaceUser;
import com.hbpu.smartpicture.model.pojo.User;
import com.hbpu.smartpicture.model.vo.space.SpaceVO;
import com.hbpu.smartpicture.model.vo.user.UserVO;
import com.hbpu.smartpicture.service.SpaceService;
import com.hbpu.smartpicture.service.SpaceUserService;
import com.hbpu.smartpicture.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author 马可
 * {@code @description} 针对表【space(空间)】的数据库操作Service实现
 * {@code @createDate} 2025-12-24 20:43:25
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {

    private final UserService userService;
    private final TransactionTemplate transactionTemplate;
    private final SpaceUserService spaceUserService;
    //    private final DynamicShardingManager dynamicShardingManager;
    // 使用 ConcurrentHashMap 来存储锁对象，每个key对应一个对象，他是线程安全的
    ConcurrentHashMap<Long, Object> longObjectConcurrentHashMap = new ConcurrentHashMap<>();

    public SpaceServiceImpl(UserService userService, TransactionTemplate transactionTemplate, @Lazy SpaceUserService spaceUserService /*@Lazy DynamicShardingManager dynamicShardingManager*/) {
        this.userService = userService;
        this.transactionTemplate = transactionTemplate;
        this.spaceUserService = spaceUserService;
//        this.dynamicShardingManager = dynamicShardingManager;
    }

    /**
     * 校验空间信息
     *
     * @param space 目标Space对象
     * @param add   是否为添加请求
     */
    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        Integer spaceType = space.getSpaceType();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        // 要创建
        if (add) {
            ThrowUtils.throwIf(StrUtil.isBlank(spaceName), ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            ThrowUtils.throwIf(spaceLevel == null, ErrorCode.PARAMS_ERROR, "空间级别不能为空");
            ThrowUtils.throwIf(spaceType == null, ErrorCode.PARAMS_ERROR, "空间类型不能为空");
        }
        // 修改数据时，如果要改空间级别
        ThrowUtils.throwIf(
                spaceType != null && SpaceTypeEnum.getEnumByValue(spaceType) == null, ErrorCode.PARAMS_ERROR,
                "空间类型不存在"
        );
        ThrowUtils.throwIf(spaceLevel != null && spaceLevelEnum == null, ErrorCode.PARAMS_ERROR, "空间级别不存在");
        ThrowUtils.throwIf(
                StrUtil.isNotBlank(spaceName) && spaceName.length() > 30,
                ErrorCode.PARAMS_ERROR,
                "空间名称过长"
        );
    }

    /**
     * 获取空间信息封装类
     *
     * @param space   目标对象
     * @param request 用户请求
     * @return 返回VO对象
     */
    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        if (space.getUserId() != null && space.getUserId() > 0) {
            UserVO userVO = userService.getUserVO(userService.getCurrentUser(request));
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }

    /**
     * 获取空间信息封装类（分页）
     *
     * @param spacePage 图片分页对象
     * @return 返回SpaceVO的分页封装类
     */
    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage) {
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(
                spacePage.getCurrent(),
                spacePage.getSize(),
                spacePage.getTotal()
        );
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVOPage;
        }
        // 对象列表 => 封装对象列表
        List<SpaceVO> spaceVOList = spaceList.stream().map(SpaceVO::objToVo).collect(Collectors.toList());
        // 1. 关联查询用户信息
        // 使用set集合去重
        Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        // 使用Map集合来进行存储，使用用户id作为key，User对象作为value，
        // 在下面进行使用的时候也不用循环遍历，而是直接根据用户id作为key直接拿到user对象，降低时间复杂度
        Map<Long, User> userIdUserListMap = userService.listByIds(userIdSet).stream()
                                                       .collect(Collectors.toMap(User::getId, user -> user));
        // 2. 填充信息
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId);
            }
            spaceVO.setUser(userService.getUserVO(user));
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }

    /**
     * 根据查询条件封装类生成查询条件
     *
     * @param spaceQueryDTO 查询条件封装类
     * @return 返回构建好的查询语句
     */
    @Override
    public LambdaQueryWrapper<Space> getQueryWrapper(SpaceQueryDTO spaceQueryDTO) {
        LambdaQueryWrapper<Space> queryWrapper = new LambdaQueryWrapper<>();
        if (spaceQueryDTO == null) {
            return queryWrapper;
        }

        // 获取封装类中的所有属性值
        Long id = spaceQueryDTO.getId();
        Long userId = spaceQueryDTO.getUserId();
        String spaceName = spaceQueryDTO.getSpaceName();
        Integer spaceLevel = spaceQueryDTO.getSpaceLevel();
        String sortField = spaceQueryDTO.getSortField();
        String sortOrder = spaceQueryDTO.getSortOrder();
        Integer spaceType = spaceQueryDTO.getSpaceType();
        // 根据属性值来生成查询条件
        queryWrapper.eq(ObjUtil.isNotEmpty(id), Space::getId, id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), Space::getUserId, userId);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), Space::getSpaceName, spaceName);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), Space::getSpaceLevel, spaceLevel);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceType), Space::getSpaceType, spaceType);
        // 5. 排序处理（防止 SQL 注入）
        //拼接字符串
        if (StrUtil.isNotBlank(sortField)) {
            String orderSql = "ORDER BY " + sortField + " " + ("ascend".equals(sortOrder) ? "ASC" : "DESC");
            queryWrapper.last(orderSql);
        }
        return queryWrapper;
    }

    /**
     * 根据目标对象的空间级别自动填充最大空间容量和数量
     *
     * @param space 目标Space对象
     */
    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        // 根据空间级别，自动填充限额
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum != null) {
            long maxSize = spaceLevelEnum.getMaxSize();
            if (space.getMaxSize() == null) {
                space.setMaxSize(maxSize);
            }
            long maxCount = spaceLevelEnum.getMaxCount();
            if (space.getMaxCount() == null) {
                space.setMaxCount(maxCount);
            }
        }
    }

    /**
     * 添加空间
     *
     * @param spaceAddDTO 添加空间的封装类
     * @param request     用户请求
     * @return 返回添加成功后的空间id
     */
    @Override
    public Long addSpace(SpaceAddDTO spaceAddDTO, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(spaceAddDTO == null, ErrorCode.PARAMS_ERROR);
        // 类型转换
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddDTO, space);
        // 给参数赋默认值
        if (StrUtil.isBlank(spaceAddDTO.getSpaceName())) {
            space.setSpaceName("默认空间");
        }
        if (spaceAddDTO.getSpaceLevel() == null) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        if (spaceAddDTO.getSpaceType() == null) {
            space.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
        }
        // 填充数据
        fillSpaceBySpaceLevel(space);
        // 进行信息校验
        validSpace(space, true);
        // 权限校验
        User currentUser = userService.getCurrentUser(request);
        space.setUserId(currentUser.getId());
        ThrowUtils.throwIf(
                SpaceLevelEnum.COMMON.getValue() != space.getSpaceLevel()
                        && !"admin".equals(currentUser.getUserRole()),
                ErrorCode.NO_AUTH_ERROR,
                "没有权限创建该级别的空间！"
        );
        // 获取用户id，如果Map中已经有其key，便返回其第一次的Object对象，如果没有则用用户id作为key，然后new 一个对象
        // 这样来保证每个用户都有一个唯一的锁，即使用户发送多次请求，但他得到的锁都是相同的，保证了锁的唯一性
        Object lock = longObjectConcurrentHashMap.computeIfAbsent(space.getUserId(), k -> new Object());
        // 使用本地锁进行加锁，避免创建用户创建多个空间
        synchronized (lock) {
            try {
                // 这里使用编程式事务，因为注解式事务回当整个方法执行完成之后在提交事务，
                // 如果同时一个用户发送了两个请求排队，上一个执行结果还没提交，就直接创建了两个空间，
                // 而编程式事务的好处就是当代码执行完成后就直接提交事务，不会等整个方法执行完成后才执行
                Long spaceID = transactionTemplate.execute(status -> {
                    // 先查询该用户已经创建了空间
                    boolean exists = this.lambdaQuery()
                                         .eq(Space::getUserId, space.getUserId())
                                         .eq(Space::getSpaceType, space.getSpaceType())
                                         .exists();
                    ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "每个用户只能创建一个私有空间和团队空间！");
                    // 执行数据库操作
                    boolean save = this.save(space);
                    ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR);
                    // 如果是团队空间，关联新增团队成员记录
                    if (SpaceTypeEnum.TEAM.getValue() == space.getSpaceType()) {
                        SpaceUser spaceUser = new SpaceUser();
                        spaceUser.setSpaceId(space.getId());
                        spaceUser.setUserId(space.getUserId());
                        spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
                        boolean result = spaceUserService.save(spaceUser);
                        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建团队成员记录失败");
                    }
                    // 动态分表（进队团队空间生效）
//                    dynamicShardingManager.createSpacePictureTable(space);
                    // 返回新写入的数据 id
                    return space.getId();
                });
                return Optional.ofNullable(spaceID).orElse(0L);
            } finally {
                // 完成后便删除这个键值对，避免内存爆炸
                longObjectConcurrentHashMap.remove(space.getUserId());
            }
        }
    }

    /**
     * 校验空间权限
     *
     * @param space   空间对象
     * @param request 用户请求
     * @return 返回校验结果
     */
    @Override
    public Boolean checkSpaceAuth(Space space, HttpServletRequest request) {
        // 获取当前用户
        User currentUser = userService.getCurrentUser(request);
        // 判断空间是否存在，以及当前用户是否是空间的创建者或者管理员
        return space != null && (space.getUserId().equals(currentUser.getId()) || "admin".equals(
                currentUser.getUserRole()));
    }
}






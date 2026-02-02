package com.hbpu.smartpicture.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hbpu.smartpicture.exception.BusinessException;
import com.hbpu.smartpicture.exception.ErrorCode;
import com.hbpu.smartpicture.exception.ThrowUtils;
import com.hbpu.smartpicture.model.dto.space.analyze.*;
import com.hbpu.smartpicture.model.pojo.Picture;
import com.hbpu.smartpicture.model.pojo.Space;
import com.hbpu.smartpicture.model.vo.space.analyze.*;
import com.hbpu.smartpicture.service.PictureService;
import com.hbpu.smartpicture.service.SpaceAnalyzeService;
import com.hbpu.smartpicture.service.SpaceService;
import com.hbpu.smartpicture.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 空间分析服务实现类
 */
@Service
public class SpaceAnalyzeServiceImpl implements SpaceAnalyzeService {
    private final UserService userService;
    private final SpaceService spaceService;
    private final PictureService pictureService;

    public SpaceAnalyzeServiceImpl(UserService userService, SpaceService spaceService, PictureService pictureService) {
        this.userService = userService;
        this.spaceService = spaceService;
        this.pictureService = pictureService;
    }


    @Override
    public SpaceUsageAnalyzeVO getSpaceUsageAnalyze(SpaceUsageAnalyzeDTO spaceUsageAnalyzeDTO, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(spaceUsageAnalyzeDTO == null, ErrorCode.PARAMS_ERROR);
        if ((spaceUsageAnalyzeDTO.isQueryAll() || spaceUsageAnalyzeDTO.isQueryPublic()) && spaceUsageAnalyzeDTO.getSpaceId() == null) {
            // 校验权限
            checkSpaceAnalyzeAuth(spaceUsageAnalyzeDTO, request);
            // 只查询图片大小字段, 用于计算空间使用情况
            QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
            pictureQueryWrapper.select("picSize");
            // 根据查询请求填充查询条件
            fillAnalyzeQueryWrapper(spaceUsageAnalyzeDTO, pictureQueryWrapper);
            List<Object> objects = pictureService.getBaseMapper().selectObjs(pictureQueryWrapper);
            // 计算已使用空间大小
            long usedSize = objects.stream().mapToLong(obj -> (Long) obj).sum();
            // 计算已使用图片数量
            long usedCount = objects.size();
            SpaceUsageAnalyzeVO spaceUsageAnalyzeVO = new SpaceUsageAnalyzeVO();
            // 设置已使用空间大小
            spaceUsageAnalyzeVO.setUsedSize(usedSize);
            // 设置已使用图片数量
            spaceUsageAnalyzeVO.setUsedCount(usedCount);
            // 公共图库五数量和容量限制，也没有比例
            spaceUsageAnalyzeVO.setMaxSize(null);
            spaceUsageAnalyzeVO.setSizeUsageRatio(null);
            spaceUsageAnalyzeVO.setMaxCount(null);
            spaceUsageAnalyzeVO.setCountUsageRatio(null);
            // 返回空间使用分析结果
            return spaceUsageAnalyzeVO;
        } else {
            // 查询私有空间的使用情况
            // 从请求参数中获取id并查询相应的space对象
            Long spaceId = spaceUsageAnalyzeDTO.getSpaceId();
            // 校验空间ID是否合法
            ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR, "空间ID不能为空");
            Space space = spaceService.getById(spaceId);
            // 校验空间是否存在
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            // 校验分析权限
            checkSpaceAnalyzeAuth(spaceUsageAnalyzeDTO, request);
            // 创建空间使用分析结果对象
            SpaceUsageAnalyzeVO spaceUsageAnalyzeVO = new SpaceUsageAnalyzeVO();
            // 设置已使用空间大小
            spaceUsageAnalyzeVO.setUsedSize(space.getTotalSize());
            // 设置最大空间大小
            spaceUsageAnalyzeVO.setMaxSize(space.getMaxSize());
            // 设置空间使用比例
            spaceUsageAnalyzeVO.setSizeUsageRatio(
                    NumberUtil.round(space.getTotalSize() * 100.0 / space.getMaxSize(), 2).doubleValue());
            // 设置已使用图片数量
            spaceUsageAnalyzeVO.setUsedCount(space.getTotalCount());
            // 设置最大图片数量
            spaceUsageAnalyzeVO.setMaxCount(space.getMaxCount());
            // 设置图片数量占比
            spaceUsageAnalyzeVO.setCountUsageRatio(
                    NumberUtil.round(space.getTotalCount() * 100.0 / space.getMaxCount(), 2).doubleValue());
            return spaceUsageAnalyzeVO;

        }
    }

    /**
     * 获取空间图片分类分析结果
     *
     * @param spaceCategoryAnalyzeDTO 空间图片分类分析参数
     * @param request                 请求
     * @return 空间图片分类分析结果
     */
    @Override
    public List<SpaceCategoryAnalyzeVO> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeDTO spaceCategoryAnalyzeDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceCategoryAnalyzeDTO == null, ErrorCode.PARAMS_ERROR);
        // 检查权限
        checkSpaceAnalyzeAuth(spaceCategoryAnalyzeDTO, request);

        // 构建查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceCategoryAnalyzeDTO, queryWrapper);

        // 使用Mybatis Plus 的分组查询
        queryWrapper.select("category", "count(*) as count", "sum(picSize) as totalSize").groupBy("category");

        // 执行查询并映射结果
        return pictureService.getBaseMapper().selectMaps(queryWrapper).stream().map(
                result -> {
                    String category = (String) result.get("category");
                    if (StrUtil.isBlank(category)) {
                        category = "未分类";
                    }
                    // 使用 Number 接收聚合函数返回值，兼容不同数据库返回类型（BigDecimal/Long等）
                    Long count = ((Number) result.get("count")).longValue();
                    Long totalSize = ((Number) result.get("totalSize")).longValue();
                    return new SpaceCategoryAnalyzeVO(category, count, totalSize);
                }).sorted((o1, o2) -> o2.getCount().compareTo(o1.getCount())).toList();
    }

    /**
     * 获取空间图片标签分析结果
     *
     * @param spaceTagAnalyzeDTO 空间图片标签分析参数
     * @param request            请求
     * @return 空间图片标签分析结果
     */
    @Override
    public List<SpaceTagAnalyzeVO> getSpaceTagAnalyze(SpaceTagAnalyzeDTO spaceTagAnalyzeDTO, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(spaceTagAnalyzeDTO == null, ErrorCode.PARAMS_ERROR);
        // 检查权限
        checkSpaceAnalyzeAuth(spaceTagAnalyzeDTO, request);
        // 构建查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceTagAnalyzeDTO, queryWrapper);
        queryWrapper.select("tags");

        // 步骤1: 从数据库查询所有图片的标签字段
        // selectObjs() 返回 List<Object>，每个元素对应一条记录的 tag 字段值
        List<String> tags = pictureService.getBaseMapper().selectObjs(queryWrapper)
                                          // 步骤2: 将查询结果转换为 Stream 流，便于后续处理
                                          .stream()
                                          // 步骤3: 过滤掉空值（null）的标签记录
                                          // 使用 Hutool 工具类的 ObjUtil.isNotNull() 判断对象是否非空
                                          .filter(ObjUtil::isNotNull)
                                          // 步骤4: 将 Object 类型转换为 String 类型
                                          // 因为数据库返回的是 Object 类型，需要转换为字符串便于解析
                                          .map(Object::toString)
                                          // 步骤5: 收集所有标签字符串到 List 集合中
                                          .toList();

        // 步骤6: 统计每个标签出现的次数
        // 注意：数据库中的 tag 字段存储的是 JSON 数组格式的字符串，如 ["标签1","标签2"]
        Map<String, Long> tagCountMap = tags.stream()
                                            // 步骤7: 将每个 JSON 数组字符串解析为标签列表，并展平成一个流
                                            // flatMap 会将多个 Stream 合并成一个 Stream
                                            // 例如：["tag1","tag2"] 和 ["tag2","tag3"]
                                            // 会被展平为：tag1, tag2, tag2, tag3
                                            .flatMap(tag -> JSONUtil.toList(tag, String.class).stream())
                                            // 步骤8: 使用收集器进行分组统计
                                            .collect(
                                                    // groupingBy: 按标签名称分组
                                                    // counting: 统计每个分组中的元素数量
                                                    // 最终得到 Map<标签名, 出现次数>
                                                    Collectors.groupingBy(tag -> tag, Collectors.counting())
                                            );


        // 步骤9: 将统计结果 Map 转换为 VO 对象列表，并按出现次数降序排列
        return tagCountMap.entrySet().stream()
                          // 步骤10: 使用 Comparator 对 Entry 进行排序
                          // Map.Entry::getValue 表示根据标签出现的次数（值）进行排序
                          // .reversed() 表示降序排列（从出现次数最多的到最少的）
                          .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                          // 步骤11: 将每个 Map.Entry 映射（转换）为 SpaceTagAnalyzeVO 对象
                          // entry.getKey() 是标签名称，entry.getValue() 是该标签出现的次数
                          .map(entry -> new SpaceTagAnalyzeVO(entry.getKey(), entry.getValue()))
                          // 步骤12: 将处理后的流收集并转换为 List 集合返回
                          .toList();
    }

    /**
     * 获取空间图片大小分析结果
     *
     * @param spaceSizeAnalyzeDTO 空间图片大小分析参数
     * @param request             请求
     * @return 空间图片大小分析结果
     */
    @Override
    public List<SpaceSizeAnalyzeVO> getSpaceSizeAnalyze(SpaceSizeAnalyzeDTO spaceSizeAnalyzeDTO, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(spaceSizeAnalyzeDTO == null, ErrorCode.PARAMS_ERROR);
        // 检查权限
        checkSpaceAnalyzeAuth(spaceSizeAnalyzeDTO, request);
        // 构建查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceSizeAnalyzeDTO, queryWrapper);

        queryWrapper.select("picSize");
        List<Long> picSizeList = pictureService.getBaseMapper().selectObjs(queryWrapper).stream().map(
                picSize -> (Long) picSize).toList();


        // 2. 预定义所有区间，确保顺序和全量
        Map<String, Long> countMap = new LinkedHashMap<>();
        countMap.put("<100KB", 0L);
        countMap.put("100KB-500KB", 0L);
        countMap.put("500KB-1MB", 0L);
        countMap.put(">1MB", 0L);

        // 3. 一次遍历归类（高效执行）
        picSizeList.forEach(size -> {
            if (size < 100 * 1024) countMap.merge("<100KB", 1L, Long::sum);
            else if (size < 500 * 1024) countMap.merge("100KB-500KB", 1L, Long::sum);
            else if (size < 1024 * 1024) countMap.merge("500KB-1MB", 1L, Long::sum);
            else countMap.merge(">1MB", 1L, Long::sum);
        });

        // 4. 转换输出
        return countMap.entrySet().stream()
                       .map(e -> new SpaceSizeAnalyzeVO(e.getKey(), e.getValue()))
                       .toList();

    }

    /**
     * 获取空间用户分析结果
     *
     * @param spaceUserAnalyzeDTO 空间用户分析参数
     * @param request             请求
     * @return 空间用户分析结果
     */
    @Override
    public List<SpaceUserAnalyzeVO> getSpaceUserAnalyze(SpaceUserAnalyzeDTO spaceUserAnalyzeDTO, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(spaceUserAnalyzeDTO == null, ErrorCode.PARAMS_ERROR);
        // 检查权限
        checkSpaceAnalyzeAuth(spaceUserAnalyzeDTO, request);
        // 构建查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceUserAnalyzeDTO, queryWrapper);
        queryWrapper.eq(ObjUtil.isNotNull(spaceUserAnalyzeDTO.getUserId()), "userId", spaceUserAnalyzeDTO.getUserId());
        // 选择时间维度，可选值：day, week, month，用于分组统计
        String timeDimension = spaceUserAnalyzeDTO.getTimeDimension();
        switch (timeDimension) {
            case "day":
                queryWrapper.select("DATE_FORMAT(createTime, '%Y-%m-%d') as period", "count(*) as count");
                break;
            case "week":
                queryWrapper.select("DATE_FORMAT(createTime, '%Y-%u') as period", "count(*) as count");
                break;
            case "month":
                queryWrapper.select("DATE_FORMAT(createTime, '%Y-%m') as period", "count(*) as count");
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "时间维度参数错误");
        }
        // 按 period 分组，并按 period 升序排列
        queryWrapper.groupBy("period").orderByAsc("period");
        // 执行查询
        List<Map<String, Object>> result = pictureService.getBaseMapper().selectMaps(queryWrapper);
        // 将查询结果转换为 SpaceUserAnalyzeVO 对象列表
        return result.stream().map(result1 -> {
            String period = (String) result1.get("period");
            Long count = (Long) result1.get("count");
            return new SpaceUserAnalyzeVO(period, count);
        }).toList();


    }


    /**
     * 检查空间分析权限
     *
     * @param spaceAnalyzeDTO 空间分析参数
     * @param request         请求
     */
    private void checkSpaceAnalyzeAuth(SpaceAnalyzeDTO spaceAnalyzeDTO, HttpServletRequest request) {
        // 检查权限
        if (spaceAnalyzeDTO.isQueryAll() || spaceAnalyzeDTO.isQueryPublic()) {
            // 全空间分析或者公共图库权限校验：仅管理员可访问
            ThrowUtils.throwIf(!userService.isAdmin(request), ErrorCode.NO_AUTH_ERROR, "无权访问公共图库");
        } else {
            // 私有空间权限校验
            Long spaceId = spaceAnalyzeDTO.getSpaceId();
            ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            // 空间权限校验
            ThrowUtils.throwIf(!spaceService.checkSpaceAuth(space, request), ErrorCode.NO_AUTH_ERROR, "无权访问该空间");
        }
    }

    /**
     * 填充空间分析查询条件
     *
     * @param spaceAnalyzeDTO 空间分析参数
     * @param queryWrapper    查询条件构造器
     */
    private static void fillAnalyzeQueryWrapper(SpaceAnalyzeDTO spaceAnalyzeDTO, QueryWrapper<Picture> queryWrapper) {
        // 检查查询参数
        // 如果查询的是整个云图库项目的图片使用量
        if (spaceAnalyzeDTO.isQueryAll()) {
            return;
        }
        // 如果查询公共空间，则选择空间id为null的图片，说明是公共图库
        if (spaceAnalyzeDTO.isQueryPublic()) {
            queryWrapper.isNull("spaceId");
            return;
        }
        // 如果查询指定空间，则选择空间id为指定空间id的
        // 图片，说明是指定私有图库中的图片
        Long spaceId = spaceAnalyzeDTO.getSpaceId();
        if (spaceId != null) {
            queryWrapper.eq("spaceId", spaceId);
            return;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "未指定查询范围");
    }


}

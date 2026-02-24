package com.hbpu.smartpicture.manager.sharding;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

/**
 * 图片分片算法
 */
public class PictureShardingAlgorithm implements StandardShardingAlgorithm<Long> {

    @Override
    public String doSharding(Collection<String> availableTargetNames,
            PreciseShardingValue<Long> preciseShardingValue) {
        Long spaceId = preciseShardingValue.getValue();
        String logicTableName = preciseShardingValue.getLogicTableName();
        // spaceId 为 null 或 0，查公共图片，走主表 picture
        if (spaceId == null || spaceId == 0) {
            return logicTableName;
        }
        // 动态分表名 picture_{spaceId}
        String realTableName = "picture_" + spaceId;
        // 如果分表已存在（DynamicShardingManager 已注册）则走分表，否则兜底走主表
        if (availableTargetNames.contains(realTableName)) {
            return realTableName;
        }
        return logicTableName;
    }

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames,
            RangeShardingValue<Long> rangeShardingValue) {
        // 范围查询暂不支持，返回空集合
        return new ArrayList<>();
    }

    @Override
    public void init(Properties properties) {
        // 无需初始化参数
    }
}
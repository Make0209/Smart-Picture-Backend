package com.hbpu.smartpicture.manager.sharding;

import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
import com.hbpu.smartpicture.exception.BusinessException;
import com.hbpu.smartpicture.model.enums.SpaceLevelEnum;
import com.hbpu.smartpicture.model.enums.SpaceTypeEnum;
import com.hbpu.smartpicture.model.pojo.Space;
import com.hbpu.smartpicture.service.SpaceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 动态分片管理器
 */
//@Component
@Slf4j
public class DynamicShardingManager {

    @Resource
    private DataSource dataSource;

    @Resource
    private SpaceService spaceService;

    private static final String LOGIC_TABLE_NAME = "picture";
    private static final String DATASOURCE_NAME = "smart_picture";
    private static final String DATABASE_NAME = "smart_picture";

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Spring Boot 启动完成，开始初始化分表规则...");
        updateShardingTableNodes();
    }

    /**
     * 更新分片规则，动态添加分表节点
     */
    public void updateShardingTableNodes() {
        Set<String> tableNames = fetchAllPictureTableNames();
        String newActualDataNodes = tableNames.stream()
                                              .map(tableName -> DATASOURCE_NAME + "." + tableName)
                                              .collect(Collectors.joining(","));
        log.info("准备更新 actualDataNodes 为: {}", newActualDataNodes);
        try {
            ContextManager contextManager = getContextManager();
            // 获取当前数据源的分片规则配置
            Collection<RuleConfiguration> currentRuleConfigs = contextManager
                    .getPersistServiceFacade()
                    .getMetaDataPersistService()
                    .getDatabaseRulePersistService().load(DATABASE_NAME);

            // 遍历当前数据源的分片规则配置
            for (RuleConfiguration config : currentRuleConfigs) {
                if (config instanceof ShardingRuleConfiguration shardingConfig) {
                    // 找到逻辑表为 picture 的分片规则配置
                    Optional<ShardingTableRuleConfiguration> targetRule = shardingConfig.getTables()
                                                                                        .stream()
                                                                                        .filter(t -> t.getLogicTable()
                                                                                                      .equals(LOGIC_TABLE_NAME))
                                                                                        .findFirst();
                    if (targetRule.isPresent()) {
                        ShardingTableRuleConfiguration oldRule = targetRule.get();
                        // 构建新的分片规则配置
                        ShardingTableRuleConfiguration newTableRule =
                                new ShardingTableRuleConfiguration(LOGIC_TABLE_NAME, newActualDataNodes);
                        newTableRule.setTableShardingStrategy(oldRule.getTableShardingStrategy());
                        newTableRule.setKeyGenerateStrategy(oldRule.getKeyGenerateStrategy());
                        newTableRule.setAuditStrategy(oldRule.getAuditStrategy());

                        // 构建新集合整体替换，避免直接操作原集合
                        List<ShardingTableRuleConfiguration> newTables = shardingConfig.getTables()
                                                                                       .stream()
                                                                                       .filter(t -> !t.getLogicTable()
                                                                                                      .equals(LOGIC_TABLE_NAME))
                                                                                       .collect(Collectors.toList());
                        // 添加新的分片规则配置
                        newTables.add(newTableRule);
                        // 构建新集合整体替换，避免直接操作原集合
                        shardingConfig.setTables(newTables);
                        // 更新分片规则配置
                        contextManager.getMetaDataContextManager()
                                      .getDatabaseRuleConfigurationManager()
                                      .alterRuleConfiguration(DATABASE_NAME, shardingConfig);
                    }
                    break;
                }
            }
//             ✅ 正确的验证方式：读运行时内存
            ContextManager newContextManager = getContextManager();
            ShardingRule shardingRule = newContextManager.getMetaDataContexts()
                                                         .getMetaData()
                                                         .getDatabase(DATASOURCE_NAME)
                                                         .getRuleMetaData()
                                                         .getSingleRule(ShardingRule.class);
            ShardingRuleConfiguration updatedConfig =
                    shardingRule.getConfiguration();
            updatedConfig.getTables().forEach(t ->
                                                      log.info(
                                                              "表:  {} 的 actualDataNodes 已更新为: {}",
                                                              t.getLogicTable(), t.getActualDataNodes()
                                                      )
            );
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("刷新分片规则异常", e);
        }
    }

    /**
     * 为指定空间创建物理分表
     *
     * @param space 空间信息
     */
    public void createSpacePictureTable(Space space) {
        if (space.getSpaceType() == SpaceTypeEnum.TEAM.getValue()
                && space.getSpaceLevel() == SpaceLevelEnum.FLAGSHIP.getValue()) {
            Long spaceId = space.getId();
            String tableName = LOGIC_TABLE_NAME + "_" + spaceId;
            String createTableSql = "CREATE TABLE IF NOT EXISTS " + tableName + " LIKE picture";
            try {
                log.info("开始为空间 {} 创建物理分表: {}", spaceId, tableName);
                SqlRunner.db().update(createTableSql);
                // 先刷新元数据感知新表，再更新路由节点
                refreshTableMetadata(tableName);
                updateShardingTableNodes();
                log.info("空间 {} 分表创建并注册成功", spaceId);
            } catch (Exception e) {
                log.error("创建图片空间分表失败，空间 id = {}, 错误: {}", spaceId, e.getMessage(), e);
                throw new RuntimeException("动态创建分表失败", e);
            }
        }
    }

    /**
     * 刷新表元数据
     *
     * @param tableName 表名
     */
    private void refreshTableMetadata(String tableName) {
        try {
            ContextManager contextManager = getContextManager();
            ShardingSphereDatabase database = contextManager.getDatabase(DATASOURCE_NAME);
            contextManager.reloadTable(database, DATASOURCE_NAME, DATASOURCE_NAME, tableName);
        } catch (Exception e) {
            log.warn("元数据预刷新失败（通常不影响后续运行）: {}", e.getMessage());
        }
    }

    /**
     * 仅查询旗舰版团队空间，与 createSpacePictureTable 的业务条件保持一致
     *
     * @return 图片分表名称集合
     */
    private Set<String> fetchAllPictureTableNames() {
        List<Space> spaceList = spaceService.lambdaQuery()
                                            .eq(Space::getSpaceType, SpaceTypeEnum.TEAM.getValue())
                                            .eq(Space::getSpaceLevel, SpaceLevelEnum.FLAGSHIP.getValue())
                                            .select(Space::getId)
                                            .list();

        Set<String> tableNames = spaceList.stream()
                                          .map(space -> LOGIC_TABLE_NAME + "_" + space.getId())
                                          .collect(Collectors.toSet());

        tableNames.add(LOGIC_TABLE_NAME);
        return tableNames;
    }

    /**
     * 获取ShardingSphere配置的上下文
     *
     * @return ContextManager
     */
    private ContextManager getContextManager() {
        try (Connection connection = dataSource.getConnection()) {
            ShardingSphereConnection shardingSphereConnection =
                    connection.unwrap(ShardingSphereConnection.class);
            return shardingSphereConnection.getContextManager();
        } catch (SQLException e) {
            throw new RuntimeException("获取 ShardingSphere ContextManager 失败", e);
        }
    }
}
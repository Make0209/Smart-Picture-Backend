package com.hbpu.smartpicture.model.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 空间
 * &#064;TableName  space
 */
@Schema(description = "空间")
@TableName(value ="space")
@Data
public class Space {
    /**
     * id
     */
    @Schema(description = "id")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 空间名称
     */
    @Schema(description = "空间名称")
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    @Schema(description = "空间级别：0-普通版 1-专业版 2-旗舰版")
    private Integer spaceLevel;

    /**
     * 空间图片的最大总大小
     */
    @Schema(description = "空间图片的最大总大小")
    private Long maxSize;

    /**
     * 空间图片的最大数量
     */
    @Schema(description = "空间图片的最大数量")
    private Long maxCount;

    /**
     * 当前空间下图片的总大小
     */
    @Schema(description = "当前空间下图片的总大小")
    private Long totalSize;

    /**
     * 当前空间下的图片数量
     */
    @Schema(description = "当前空间下的图片数量")
    private Long totalCount;

    /**
     * 创建用户 id
     */
    @Schema(description = "创建用户 id")
    private Long userId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 编辑时间
     */
    @Schema(description = "编辑时间")
    private LocalDateTime editTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @Schema(description = "是否删除")
    @TableLogic
    private Integer isDelete;
}
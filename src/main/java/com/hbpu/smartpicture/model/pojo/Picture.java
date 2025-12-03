package com.hbpu.smartpicture.model.pojo;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 图片
 * &#064;TableName  picture
 */
@Schema(description = "图片")
@TableName(value = "picture")
@Data
public class Picture {
    /**
     * id
     */
    @Schema(description = "id")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 图片 url
     */
    @Schema(description = "图片 url")
    private String url;

    /**
     * 图片名称
     */
    @Schema(description = "图片名称")
    private String name;

    /**
     * 简介
     */
    @Schema(description = "简介")
    private String introduction;

    /**
     * 分类
     */
    @Schema(description = "分类")
    private String category;

    /**
     * 标签（JSON 数组）
     */
    @Schema(description = "标签（JSON 数组）")
    private String tags;

    /**
     * 图片体积
     */
    @Schema(description = "图片体积")
    private Long picSize;

    /**
     * 图片宽度
     */
    @Schema(description = "图片宽度")
    private Integer picWidth;

    /**
     * 图片高度
     */
    @Schema(description = "图片高度")
    private Integer picHeight;

    /**
     * 图片宽高比例
     */
    @Schema(description = "图片宽高比例")
    private Double picScale;

    /**
     * 图片格式
     */
    @Schema(description = "图片格式")
    private String picFormat;

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
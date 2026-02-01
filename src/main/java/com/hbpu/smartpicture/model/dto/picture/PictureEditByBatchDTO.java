package com.hbpu.smartpicture.model.dto.picture;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 批量编辑图片信息请求封装类
 */
@Schema(description = "批量编辑图片信息请求封装类")
@Data
public class PictureEditByBatchDTO implements Serializable {
    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = -6727580588664174797L;

    /**
     * 图片 id 列表
     */
    @Schema(description = "图片 id 列表")
    private List<Long> pictureIdList;

    /**
     * 空间 id
     */
    @Schema(description = "空间 id")
    private Long spaceId;

    /**
     * 分类
     */
    @Schema(description = "分类")
    private String category;

    /**
     * 标签
     */
    @Schema(description = "标签")
    private List<String> tags;

    /**
     * 命名规则
     */
    @Schema(description = "命名规则")
    private String nameRule;

}


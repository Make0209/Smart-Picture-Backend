package com.hbpu.smartpicture.model.vo.space.analyze;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 空间图片分类分析结果封装类
 */
@Schema(description = "空间图片分类分析结果封装类")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceCategoryAnalyzeVO implements Serializable {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = 1482576852134974175L;

    /**
     * 图片分类
     */
    @Schema(description = "图片分类")
    private String category;

    /**
     * 图片数量
     */
    @Schema(description = "图片数量")
    private Long count;

    /**
     * 分类图片总大小
     */
    @Schema(description = "分类图片总大小")
    private Long totalSize;
}


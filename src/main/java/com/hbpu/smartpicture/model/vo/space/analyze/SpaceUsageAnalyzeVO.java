package com.hbpu.smartpicture.model.vo.space.analyze;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 空间使用分析参数响应封装类
 */
@Schema(description = "空间使用分析参数响应封装类")
@Data
public class SpaceUsageAnalyzeVO implements Serializable {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = 7503192895988342471L;

    /**
     * 已使用大小
     */
    @Schema(description = "已使用大小")
    private Long usedSize;

    /**
     * 总大小
     */
    @Schema(description = "总大小")
    private Long maxSize;

    /**
     * 空间使用比例
     */
    @Schema(description = "空间使用比例")
    private Double sizeUsageRatio;

    /**
     * 当前图片数量
     */
    @Schema(description = "当前图片数量")
    private Long usedCount;

    /**
     * 最大图片数量
     */
    @Schema(description = "最大图片数量")
    private Long maxCount;

    /**
     * 图片数量占比
     */
    @Schema(description = "图片数量占比")
    private Double countUsageRatio;

}

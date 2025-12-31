package com.hbpu.smartpicture.model.vo.space;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 空间级别菜单响应封装类
 */
@Schema(description = "空间级别菜单响应封装类")
@Data
@AllArgsConstructor
public class SpaceLevelVO implements Serializable {
    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = 6322539324136757341L;

    /**
     * 值
     */
    @Schema(description = "值")
    private int value;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String text;

    /**
     * 最大数量
     */
    @Schema(description = "最大数量")
    private long maxCount;

    /**
     * 最大容量
     */
    @Schema(description = "最大容量")
    private long maxSize;
}

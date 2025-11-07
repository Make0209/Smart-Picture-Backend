package com.hbpu.smartpicture.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分页请求包装类
 */
@Schema(description = "分页请求包装类")
@Data
public class PageRequest implements Serializable {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = -3993675350518155062L;

    /**
     * 当前页号
     */
    @Schema(description = "当前页号")
    private int current = 1;

    /**
     * 页面大小
     */
    @Schema(description = "页面大小")
    private int pageSize = 10;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段")
    private String sortField;

    /**
     * 排序顺序（默认降序）
     */
    @Schema(description = "排序顺序（默认降序）")
    private String sortOrder = "descend";
}

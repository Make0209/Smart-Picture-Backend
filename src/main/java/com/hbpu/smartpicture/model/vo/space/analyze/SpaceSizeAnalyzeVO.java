package com.hbpu.smartpicture.model.vo.space.analyze;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 空间图片大小分析结果封装类
 */
@Schema(description = "空间图片大小分析结果封装类")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceSizeAnalyzeVO implements Serializable {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = 6285381589057861350L;

    /**
     * 图片大小范围
     */
    @Schema(description = "图片大小范围")
    private String sizeRange;

    /**
     * 图片数量
     */
    @Schema(description = "图片数量")
    private Long count;


}


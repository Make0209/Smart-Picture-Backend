package com.hbpu.smartpicture.model.vo.space.analyze;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 空间图片标签分析请求封装类
 */
@Schema(description = "空间图片标签分析请求封装类")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceTagAnalyzeVO implements Serializable {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = -2835995135231557871L;

    /**
     * 标签名称
     */
    @Schema(description = "标签名称")
    private String tag;

    /**
     * 使用次数
     */
    @Schema(description = "使用次数")
    private Long count;

}


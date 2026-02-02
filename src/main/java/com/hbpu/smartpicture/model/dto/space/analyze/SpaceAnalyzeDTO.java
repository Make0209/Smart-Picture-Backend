package com.hbpu.smartpicture.model.dto.space.analyze;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 空间分析请求参数
 *
 */
@Schema(description = "空间分析请求参数")
@Data
public class SpaceAnalyzeDTO implements Serializable {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = -8646906293595789415L;

    /**
     * 空间 ID
     */
    @Schema(description = "空间 ID")
    private Long spaceId;

    /**
     * 是否查询公共图库
     */
    @Schema(description = "是否查询公共图库")
    private boolean queryPublic;

    /**
     * 全空间分析
     */
    @Schema(description = "全空间分析")
    private boolean queryAll;

}


package com.hbpu.smartpicture.model.dto.space.analyze;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 空间使用排行分析请求参数
 */
@Schema(description = "空间使用排行分析请求参数")
@Data
public class SpaceRankAnalyzeDTO implements Serializable {


    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = -8656031847721613162L;

    /**
     * 排名前 N 的空间
     */
    @Schema(description = "排名前 N 的空间")
    private Integer topN = 10;

}

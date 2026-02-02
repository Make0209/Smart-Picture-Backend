package com.hbpu.smartpicture.model.dto.space.analyze;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 空间图片大小分析请求参数封装类
 */
@Schema(description = "空间图片大小分析请求参数封装类")
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceSizeAnalyzeDTO extends SpaceAnalyzeDTO {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = 451255402731753292L;

}


package com.hbpu.smartpicture.model.dto.space.analyze;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 空间图片分类分析请求封装类
 */
@Schema(description = "空间图片分类分析请求封装类")
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceCategoryAnalyzeDTO extends SpaceAnalyzeDTO {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = -1764598241566970046L;

}


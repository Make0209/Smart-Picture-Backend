package com.hbpu.smartpicture.model.dto.space.analyze;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 空间用户上传行为分析请求封装类
 */
@Schema(description = "空间用户上传行为分析请求封装类")
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceUserAnalyzeDTO extends SpaceAnalyzeDTO {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = -4259219351222502751L;

    /**
     * 用户 ID
     */
    @Schema(description = "用户 ID")
    private Long userId;

    /**
     * 时间维度：day / week / month
     */
    @Schema(description = "时间维度：day / week / month")
    private String timeDimension;
}

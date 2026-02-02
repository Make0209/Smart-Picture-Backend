package com.hbpu.smartpicture.model.vo.space.analyze;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 空间用户上传行为分析结果封装类
 */
@Schema(description = "空间用户上传行为分析结果封装类")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceUserAnalyzeVO implements Serializable {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = 6112290703443885675L;

    /**
     * 时间区间
     */
    @Schema(description = "时间区间")
    private String period;

    /**
     * 上传数量
     */
    @Schema(description = "上传数量")
    private Long count;


}


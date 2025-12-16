package com.hbpu.smartpicture.model.dto.picture;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 图片审核请求封装类
 */
@Schema(description = "图片审核请求封装类")
@Data
public class PictureReviewDTO implements Serializable {
    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = 4069286815948870030L;

    /**
     * id
     */
    @Schema(description = "id")
    private Long id;

    /**
     * 审核状态：0-待审核; 1-通过; 2-拒绝
     */
    @Schema(description = "审核状态：0-待审核; 1-通过; 2-拒绝")
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    @Schema(description = "审核信息")
    private String reviewMessage;
}

package com.hbpu.smartpicture.model.dto.picture;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 颜色搜图请求封装类
 */
@Schema(description = "颜色搜图请求封装类")
@Data
public class SearchPictureByColorDTO implements Serializable {
    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = -5225873379451601927L;

    /**
     * 图片主色调
     */
    @Schema(description = "图片主色调")
    private String picColor;

    /**
     * 空间 id
     */
    @Schema(description = "空间 id")
    private Long spaceId;
}


package com.hbpu.smartpicture.model.dto.picture;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 以图搜图请求封装类
 */
@Schema(description = "以图搜图请求封装类")
@Data
public class SearchPictureByPictureDTO implements Serializable {
    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = 4263397011035805172L;

    /**
     * 图片id
     */
    @Schema(description = "图片id")
    private Long pictureId;
}

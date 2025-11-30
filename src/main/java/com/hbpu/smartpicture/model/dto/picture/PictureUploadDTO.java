package com.hbpu.smartpicture.model.dto.picture;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 上传图片的请求封装类
 */
@Schema(description = "上传图片的请求封装类")
@Data
public class PictureUploadDTO implements Serializable {
    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = 7933043792037666171L;

    /**
     * 图片id
     */
    @Schema(description = "图片id")
    private Long id;
}

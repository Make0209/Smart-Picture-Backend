package com.hbpu.smartpicture.model.dto.picture;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Schema
@Data
public class PictureEditDTO implements Serializable {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = 594148912981780012L;

    /**
     * id
     */
    @Schema(description = "id")
    private Long id;

    /**
     * 图片名称
     */
    @Schema(description = "图片名称")
    private String name;

    /**
     * 简介
     */
    @Schema(description = "简介")
    private String introduction;

    /**
     * 分类
     */
    @Schema(description = "分类")
    private String category;

    /**
     * 标签
     */
    @Schema(description = "标签")
    private List<String> tags;

}

package com.hbpu.smartpicture.model.vo.picture;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 返回给前端的标签分类封装类
 */
@Schema(description = "返回给前端的标签分类封装类")
@Data
public class PictureTagCategoryVO implements Serializable {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = -4502928851700266783L;

    /**
     * 标签列表
     */
    @Schema(description = "标签列表")
    private List<String> tagList;

    /**
     * 分类列表
     */
    @Schema(description = "分类列表")
    private List<String> categoryList;

}

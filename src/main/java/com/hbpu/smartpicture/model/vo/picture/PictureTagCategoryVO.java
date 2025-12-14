package com.hbpu.smartpicture.model.vo.picture;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 返回给前端的标签分类封装类
 */
@Data
public class PictureTagCategoryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -4502928851700266783L;

    /**
     * 标签列表
     */
    private List<String> tagList;

    /**
     * 分类列表
     */
    private List<String> categoryList;

}

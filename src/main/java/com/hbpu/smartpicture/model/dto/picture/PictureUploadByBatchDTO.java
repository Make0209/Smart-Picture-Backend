package com.hbpu.smartpicture.model.dto.picture;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 批量导入图片请求封装类
 */
@Schema(description = "批量导入图片请求封装类")
@Data
public class PictureUploadByBatchDTO implements Serializable {
    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = -8104207967991777294L;

    /**
     * 搜索关键词
     */
    @Schema(description = "搜索关键词")
    private String searchText;

    /**
     * 抓取数量
     */
    @Schema(description = "抓取数量")
    private Integer count = 10;

    /**
     * 图片保存前缀
     */
    @Schema(description = "图片保存前缀")
    private String namePrefix;
}

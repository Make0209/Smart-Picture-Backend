package com.hbpu.smartpicture.api.imagesearch.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 图片搜索结果封装类
 */
@Schema(description = "图片搜索结果封装类")
@Data
public class ImageSearchResult {

    /**
     * 缩略图地址
     */
    @Schema(description = "缩略图地址")
    private String thumbUrl;

    /**
     * 来源地址
     */
    @Schema(description = "来源地址")
    private String fromUrl;
}


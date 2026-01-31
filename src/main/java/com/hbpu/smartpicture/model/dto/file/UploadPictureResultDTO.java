package com.hbpu.smartpicture.model.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 上传图片后返回图片相关信息的封装类
 */
@Schema(description = "上传图片后返回图片相关信息的封装类")
@Data
public class UploadPictureResultDTO {

    /**
     * 图片地址
     */
    @Schema(description = "图片地址")
    private String url;

    /**
     * 图片名称
     */
    @Schema(description = "图片名称")
    private String name;

    /**
     * 文件体积
     */
    @Schema(description = "文件体积")
    private Long picSize;

    /**
     * 图片宽度
     */
    @Schema(description = "图片宽度")
    private int picWidth;

    /**
     * 图片高度
     */
    @Schema(description = "图片高度")
    private int picHeight;

    /**
     * 图片宽高比
     */
    @Schema(description = "图片宽高比")
    private Double picScale;

    /**
     * 图片格式
     */
    @Schema(description = "图片格式")
    private String picFormat;

    /**
     * 缩略图url
     */
    @Schema(description = "缩略图url")
    private String thumbnailUrl;

    /**
     * 图片主色调
     */
    @Schema(description = "图片主色调")
    private String picColor;
}

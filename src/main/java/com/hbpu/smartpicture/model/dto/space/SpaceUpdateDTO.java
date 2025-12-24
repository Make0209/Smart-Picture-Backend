package com.hbpu.smartpicture.model.dto.space;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 更新空间信息封装类
 */
@Schema(description = "更新空间信息封装类")
@Data
public class SpaceUpdateDTO implements Serializable {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = -1146190093185094542L;

    /**
     * id
     */
    @Schema(description = "id")
    private Long id;

    /**
     * 空间名称
     */
    @Schema(description = "空间名称")
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    @Schema(description = "空间级别：0-普通版 1-专业版 2-旗舰版")
    private Integer spaceLevel;

    /**
     * 空间图片的最大总大小
     */
    @Schema(description = "空间图片的最大总大小")
    private Long maxSize;

    /**
     * 空间图片的最大数量
     */
    @Schema(description = "空间图片的最大数量")
    private Long maxCount;


}


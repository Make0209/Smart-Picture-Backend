package com.hbpu.smartpicture.model.dto.space;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 新建空间请求封装类
 */
@Schema(description = "新建空间请求封装类")
@Data
public class SpaceAddDTO implements Serializable {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = 1716587126463483353L;

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

}


package com.hbpu.smartpicture.model.dto.space;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 编辑空间请求封装类
 */
@Schema(description = "编辑空间请求封装类")
@Data
public class SpaceEditDTO implements Serializable {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = -3025769394063174070L;


    /**
     * 空间 id
     */
    @Schema(description = "空间 id")
    private Long id;

    /**
     * 空间名称
     */
    @Schema(description = "空间名称")
    private String spaceName;


}


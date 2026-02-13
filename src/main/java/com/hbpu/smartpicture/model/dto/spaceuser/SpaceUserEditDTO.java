package com.hbpu.smartpicture.model.dto.spaceuser;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 空间用户编辑参数
 */
@Schema(description = "空间用户编辑参数")
@Data
public class SpaceUserEditDTO implements Serializable {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = -6642513484956875674L;

    /**
     * id
     */
    @Schema(description = "id")
    private Long id;

    /**
     * 空间角色：viewer/editor/admin
     */
    @Schema(description = "空间角色：viewer/editor/admin")
    private String spaceRole;

}


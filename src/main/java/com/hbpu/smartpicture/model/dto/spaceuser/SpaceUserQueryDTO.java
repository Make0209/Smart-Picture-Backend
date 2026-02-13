package com.hbpu.smartpicture.model.dto.spaceuser;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 空间用户查询参数
 */
@Schema(description = "空间用户查询参数")
@Data
public class SpaceUserQueryDTO implements Serializable {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = 7434873454914873638L;

    /**
     * ID
     */
    @Schema(description = "ID")
    private Long id;

    /**
     * 空间 ID
     */
    @Schema(description = "空间 ID")
    private Long spaceId;

    /**
     * 用户 ID
     */
    @Schema(description = "用户 ID")
    private Long userId;

    /**
     * 空间角色：viewer/editor/admin
     */
    @Schema(description = "空间角色：viewer/editor/admin")
    private String spaceRole;


}


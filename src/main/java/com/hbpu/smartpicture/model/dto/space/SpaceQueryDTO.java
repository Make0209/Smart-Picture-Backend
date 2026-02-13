package com.hbpu.smartpicture.model.dto.space;

import com.hbpu.smartpicture.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分页查询空间请求封装类
 */
@Schema(description = "分页查询空间请求封装类")
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceQueryDTO extends PageRequest implements Serializable {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = 5249155177333868285L;
    /**
     * id
     */
    @Schema(description = "id")
    private Long id;

    /**
     * 用户 id
     */
    @Schema(description = "用户 id")
    private Long userId;

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
     * 空间类型：0-私有 1-团队
     */
    @Schema(description = "空间类型：0-私有 1-团队")
    private Integer spaceType;

}


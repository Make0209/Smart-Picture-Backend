package com.hbpu.smartpicture.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用删除请求包装类
 */
@Schema(description = "通用删除请求包装类")
@Data
public class DeleteRequest implements Serializable {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = 4038630039960713304L;
    /**
     * id
     */
    @Schema(description = "id")
    private Long id;


}


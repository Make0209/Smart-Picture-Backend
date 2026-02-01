package com.hbpu.smartpicture.model.dto.picture;

import com.hbpu.smartpicture.api.aliyunai.model.CreateOutPaintingTaskRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 创建图片扩图任务参数封装类
 */
@Schema(description = "创建图片扩图任务参数封装类")
@Data
public class CreatePictureOutPaintingTaskDTO implements Serializable {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = -963246658118845924L;

    /**
     * 图片 id
     */
    @Schema(description = "图片 id")
    private Long pictureId;

    /**
     * 扩图参数
     */
    @Schema(description = "扩图参数")
    private CreateOutPaintingTaskRequest.Parameters parameters;

}

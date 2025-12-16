package com.hbpu.smartpicture.model.enums;

import cn.hutool.core.util.ObjUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 图片审核状态枚举类
 */
@Schema(description = "图片审核状态枚举类")
@Getter
public enum PictureReviewStatusEnum {

    @Schema(hidden = true) REVIEWING("待审核", 0),
    @Schema(hidden = true) PASS("通过", 1),
    @Schema(hidden = true) REJECT("拒绝", 2);

    @Schema(hidden = true)
    private final String text;
    @Schema(hidden = true)
    private final int value;

    PictureReviewStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value获取枚举值
     *
     * @param value 枚举值value
     * @return 对应的枚举值
     */
    public static PictureReviewStatusEnum findByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (PictureReviewStatusEnum pictureReviewStatusEnum : PictureReviewStatusEnum.values()) {
            if (pictureReviewStatusEnum.value == value) {
                return pictureReviewStatusEnum;
            }
        }
        return null;
    }
}


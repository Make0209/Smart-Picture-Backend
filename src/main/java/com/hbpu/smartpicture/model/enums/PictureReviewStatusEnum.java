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

    /**
     * 待审核状态，值为0
     */
    @Schema(hidden = true) REVIEWING("待审核", 0),

    /**
     * 审核通过状态，值为1
     */
    @Schema(hidden = true) PASS("通过", 1),

    /**
     * 审核拒绝状态，值为2
     */
    @Schema(hidden = true) REJECT("拒绝", 2);

    /**
     * 枚举项的文本描述
     */
    @Schema(hidden = true)
    private final String text;

    /**
     * 枚举项的值
     */
    @Schema(hidden = true)
    private final int value;

    /**
     * 私有构造函数，用于初始化枚举项
     *
     * @param text  枚举项的文本描述
     * @param value 枚举项的值
     */
    PictureReviewStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据枚举值获取对应的枚举项
     *
     * @param value 枚举值
     * @return 对应的枚举项，如果未找到则返回null
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


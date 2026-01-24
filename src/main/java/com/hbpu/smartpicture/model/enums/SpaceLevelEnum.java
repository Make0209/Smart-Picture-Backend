package com.hbpu.smartpicture.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 空间等级枚举类，定义了不同空间等级的属性和方法。
 */
@Getter
public enum SpaceLevelEnum {

    /**
     * 普通版，最大图片数量为100，最大图片总大小为100MB。
     */
    COMMON("普通版", 0, 100, 100L * 1024 * 1024),

    /**
     * 专业版，最大图片数量为1000，最大图片总大小为1GB。
     */
    PROFESSIONAL("专业版", 1, 1000, 1000L * 1024 * 1024),

    /**
     * 旗舰版，最大图片数量为10000，最大图片总大小为10GB。
     */
    FLAGSHIP("旗舰版", 2, 10000, 10000L * 1024 * 1024);

    /**
     * 文本描述
     */
    private final String text;

    /**
     * 值，用于标识不同的空间等级
     */
    private final int value;

    /**
     * 最大图片总数量
     */
    private final long maxCount;

    /**
     * 最大图片总大小（以字节为单位）
     */
    private final long maxSize;


    /**
     * 构造函数，初始化枚举值。
     *
     * @param text     文本描述
     * @param value    值，用于标识不同的空间等级
     * @param maxCount 最大图片总数量
     * @param maxSize  最大图片总大小（以字节为单位）
     */
    SpaceLevelEnum(String text, int value, long maxCount, long maxSize) {
        this.text = text;
        this.value = value;
        this.maxCount = maxCount;
        this.maxSize = maxSize;
    }

    /**
     * 根据值获取枚举对象。
     *
     * @param value 值，用于标识不同的空间等级
     * @return 对应的枚举对象，如果不存在则返回null
     */
    public static SpaceLevelEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (SpaceLevelEnum spaceLevelEnum : SpaceLevelEnum.values()) {
            if (spaceLevelEnum.value == value) {
                return spaceLevelEnum;
            }
        }
        return null;
    }
}


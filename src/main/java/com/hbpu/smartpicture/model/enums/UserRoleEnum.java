package com.hbpu.smartpicture.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 用户角色枚举类，定义了不同用户角色的属性和方法。
 */
@Getter
public enum UserRoleEnum {

    /**
     * 普通用户角色，标识普通用户。
     */
    USER("用户", "user"),

    /**
     * 管理员角色，标识系统管理员。
     */
    ADMIN("管理员", "admin");

    /**
     * 角色的文本描述
     */
    private final String text;

    /**
     * 角色的值，用于标识不同的用户角色
     */
    private final String value;


    /**
     * 构造函数，初始化枚举值。
     *
     * @param text  文本描述
     * @param value 值，用于标识不同的用户角色
     */
    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据值获取枚举对象。
     *
     * @param value 值，用于标识不同的用户角色
     * @return 对应的枚举对象，如果不存在则返回null
     */
    public static UserRoleEnum findByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (UserRoleEnum item : UserRoleEnum.values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        return null;
    }
}

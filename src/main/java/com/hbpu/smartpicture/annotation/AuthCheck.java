package com.hbpu.smartpicture.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)//表示对哪种类型生效，当前表示对方法生效
@Retention(RetentionPolicy.RUNTIME)//在什么时候生效，当前表示运行时生效
public @interface AuthCheck {

    /**
     * 必须具有的某个角色
     * @return 返回权限值
     */
    String mustRole() default " ";
}

package com.hbpu.smartpicture.model.dto.space.analyze;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
/**
 * 空间使用分析参数
 */

/**
 * &#064;EqualsAndHashCode(callSuper  = true) 的作用：
 * 1. 生成 equals(Object other) 和 hashCode() 方法。
 * 2. callSuper = true 表示在计算 equals 和 hashCode 时，会调用父类（SpaceAnalyzeDTO）的属性。
 * 3. 如果不加此注解或设置为 false，当两个对象的父类字段不同但子类字段相同时，equals 可能会返回 true，导致逻辑错误。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceUsageAnalyzeDTO extends SpaceAnalyzeDTO {
    @Serial
    private static final long serialVersionUID = 6419159193016751265L;

}

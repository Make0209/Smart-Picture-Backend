package com.hbpu.smartpicture.utils;

/**
 * 颜色值转换工具类
 */
public class ColorTransformUtils {
    public ColorTransformUtils() {
    }

    /**
     * 颜色转换方法，确保颜色正确
     *
     * @param hexColor 颜色值字符串
     * @return 处理后的颜色值字符串
     */
    public static String colorTransform(String hexColor) {
        // 获取颜色值字符串
        int color;
        // 移除 "0x" 或 "0X" 前缀
        if (hexColor.startsWith("0x") || hexColor.startsWith("0X")) {
            hexColor = hexColor.substring(2);
        }
        // 将十六进制字符串转换为整数
        color = Integer.parseInt(hexColor, 16);  // 指定基数为16
        // 使用格式化补零，确保总是6位
        String picColor = String.format("%06X", color);
        return "0x" + picColor;
    }
}

package com.hbpu.smartpicture.api.imagesearch.sub;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.hbpu.smartpicture.exception.BusinessException;
import com.hbpu.smartpicture.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 获取图片搜索页面地址类
 */
@Slf4j
public class GetImagePageUrlApi {

    /**
     * 获取以图搜图页面地址
     *
     * @param imageUrl 目标图片url
     * @return 返回以图搜图的页面地址
     */
    public static String getImagePageUrl(String imageUrl) {
        String encode = URLEncoder.encode(imageUrl, StandardCharsets.UTF_8);
        // 1. 准备请求参数
        Map<String, Object> formData = new HashMap<>();
        formData.put("image", imageUrl);
        formData.put("tn", "pc");
        formData.put("from", "pc");
        formData.put("image_source", "PC_UPLOAD_URL");
        // 获取当前时间戳
        Instant now = Instant.now();
        long uptime = now.toEpochMilli();
        // 请求地址
        String url = "https://graph.baidu.com/upload?uptime=" + uptime;

        try {
            // 2. 发送 POST 请求到百度接口
            HttpResponse response = HttpRequest.post(url)
                                               // 关键：添加 acs-token 请求头，否则返回"系统异常"
                                               .header("acs-token", RandomUtil.randomString(10))
                                               // 建议：添加 User-Agent 伪装浏览器
                                               .header(
                                                       "User-Agent",
                                                       "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
                                               )
                                               .header("Referer", "https://graph.baidu.com/")
                                               .form(formData)
                                               .timeout(5000)
                                               .execute();
            // 判断响应状态
            if (HttpStatus.HTTP_OK != response.getStatus()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
            // 解析响应
            String responseBody = response.body();
            Map<String, Object> result = JSONUtil.toBean(responseBody, Map.class);

            // 3. 处理响应结果
            if (result == null || !Integer.valueOf(0).equals(result.get("status"))) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            String rawUrl = (String) data.get("url");
            // 对 URL 进行解码
            String searchResultUrl = URLUtil.decode(rawUrl, StandardCharsets.UTF_8);
            // 如果 URL 为空
            if (searchResultUrl == null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "未返回有效结果");
            }
            return searchResultUrl;
        } catch (Exception e) {
            log.error("搜索失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜索失败");
        }
    }

    public static void main(String[] args) {
        // 测试以图搜图功能
        String imageUrl = "https://mms-graph.cdn.bcebos.com/home-pc/scan.jpeg";
        String result = getImagePageUrl(imageUrl);
        System.out.println("搜索成功，结果 URL：" + result);
    }
}


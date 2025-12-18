package com.hbpu.smartpicture.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.hbpu.smartpicture.exception.BusinessException;
import com.hbpu.smartpicture.exception.ErrorCode;
import com.hbpu.smartpicture.exception.ThrowUtils;
import com.hbpu.smartpicture.manager.CosManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@Slf4j
@Service
public class UrlPictureUpload extends PictureUploadTemplate {
    public UrlPictureUpload(CosManager cosManager) {
        super(cosManager);
    }

    /**
     * 获取原始文件名
     * @param inputSource 输入源
     * @return 返回原始文件名（确保带有扩展名）
     */
    @Override
    protected String getOriginalFilename(Object inputSource) {
        if (!(inputSource instanceof String fileUrl)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String fileName = FileUtil.getName(fileUrl);
        String extension = FileUtil.getSuffix(fileName);
        // 如果已有有效的图片扩展名，直接返回
        if (isValidImageExtension(extension)) {
            return fileName;
        }
        // 没有扩展名或不是有效图片扩展名，需要检测
        extension = detectExtensionFromUrl(fileUrl);
        // 处理特殊的文件名（如必应图片ID）
        fileName = sanitizeFileName(fileName);

        return fileName + "." + extension;
    }

    /**
     * 清理和规范化文件名
     */
    private String sanitizeFileName(String fileName) {
        // 移除已存在的无效扩展名
        fileName = FileUtil.getPrefix(fileName);
        // 如果是类似 OIP-C.CLtPLHNphFkQtFRhzLLTQAHaE1 这种特殊ID
        // 或文件名过长，生成新的文件名
        if (fileName.length() > 50 || fileName.matches("^OIP-[A-Z]\\..*")) {
            // 使用时间戳+随机字符串生成新文件名
            return "img_" + System.currentTimeMillis() + "_" + RandomUtil.randomString(8);
        }
        // 移除特殊字符，只保留字母数字和部分符号
        fileName = fileName.replaceAll("[^a-zA-Z0-9_-]", "_");
        return fileName;
    }

    /**
     * 验证是否为有效的图片扩展名
     */
    private boolean isValidImageExtension(String extension) {
        if (StrUtil.isBlank(extension)) {
            return false;
        }
        Set<String> validExtensions = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp", "svg", "ico");
        return validExtensions.contains(extension.toLowerCase());
    }
    /**
     * 通过HTTP请求检测文件扩展名
     */
    private String detectExtensionFromUrl(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            String contentType = connection.getContentType();
            connection.disconnect();
            return parseExtensionFromContentType(contentType);
        } catch (Exception e) {
            log.warn("无法检测文件类型，使用默认扩展名: {}", fileUrl, e);
            return "jpg";
        }
    }

    /**
     * 从Content-Type解析扩展名
     */
    private String parseExtensionFromContentType(String contentType) {
        if (StrUtil.isBlank(contentType)) {
            return "jpg";
        }
        contentType = contentType.split(";")[0].trim().toLowerCase();
        Map<String, String> mimeTypeMap = new HashMap<>();
        mimeTypeMap.put("image/jpeg", "jpg");
        mimeTypeMap.put("image/jpg", "jpg");
        mimeTypeMap.put("image/png", "png");
        mimeTypeMap.put("image/gif", "gif");
        mimeTypeMap.put("image/webp", "webp");
        mimeTypeMap.put("image/bmp", "bmp");
        mimeTypeMap.put("image/svg+xml", "svg");
        return mimeTypeMap.getOrDefault(contentType, "jpg");
    }

    @Override
    protected MultipartFile processFile(Object inputSource) {
        if (inputSource instanceof String) {
            String fileName = getOriginalFilename(inputSource);
            MultipartFile file;
            try (InputStream inputStream = HttpUtil.createGet(inputSource.toString())
                                                   .timeout(5000) // 设置超时 5 秒
                                                   .execute()
                                                   .bodyStream();) {
                // 根据文件输入流来判断文件类型
                String contentType;
                try (HttpResponse response = HttpUtil.createRequest(Method.HEAD, inputSource.toString()).execute()) {
                    contentType = response.header("Content-Type");
                }
                file = new MockMultipartFile("file", fileName, contentType, inputStream);
            } catch (IOException e) {
                log.error("获取文件类型或文件流时出现错误{}", e.getMessage());
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
            return file;
        } else {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Override
    protected void validPicture(Object inputSource) {
        if (inputSource instanceof String fileUrl) {
            ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址不能为空");
            try {
                // 1. 验证 URL 格式
                new URL(fileUrl); // 验证是否是合法的 URL
            } catch (MalformedURLException e) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
            }
            // 2. 校验 URL 协议
            ThrowUtils.throwIf(
                    !(fileUrl.startsWith("http://") || fileUrl.startsWith("https://")),
                    ErrorCode.PARAMS_ERROR, "仅支持 HTTP 或 HTTPS 协议的文件地址"
            );

            // 3. 发送 HEAD 请求以验证文件是否存在
            try (HttpResponse response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute()) {
                // 未正常返回，无需执行其他判断
                if (response.getStatus() != HttpStatus.HTTP_OK) {
                    return;
                }
                // 4. 校验文件类型
                String contentType = response.header("Content-Type");
                if (StrUtil.isNotBlank(contentType)) {
                    // 允许的图片类型
                    final List<String> ALLOW_CONTENT_TYPES = Arrays.asList(
                            "image/jpeg", "image/jpg", "image/png", "image/webp");
                    ThrowUtils.throwIf(
                            !ALLOW_CONTENT_TYPES.contains(contentType.toLowerCase()),
                            ErrorCode.PARAMS_ERROR, "文件类型错误"
                    );
                }
                // 5. 校验文件大小
                String contentLengthStr = response.header("Content-Length");
                if (StrUtil.isNotBlank(contentLengthStr)) {
                    try {
                        long contentLength = Long.parseLong(contentLengthStr);
                        final long TWO_MB = 2 * 1024 * 1024L; // 限制文件大小为 2MB
                        ThrowUtils.throwIf(contentLength > TWO_MB, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2M");
                    } catch (NumberFormatException e) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式错误");
                    }
                }
            }
        }
    }
}

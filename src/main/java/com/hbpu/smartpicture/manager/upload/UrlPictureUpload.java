package com.hbpu.smartpicture.manager.upload;

import cn.hutool.core.io.FileUtil;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class UrlPictureUpload extends PictureUploadTemplate {
    public UrlPictureUpload(CosManager cosManager) {
        super(cosManager);
    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        if (inputSource instanceof String fileUrl) {
            return FileUtil.getName(fileUrl);
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
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

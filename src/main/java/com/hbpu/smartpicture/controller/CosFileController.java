package com.hbpu.smartpicture.controller;

import cn.hutool.core.util.StrUtil;
import com.hbpu.smartpicture.annotation.AuthCheck;
import com.hbpu.smartpicture.common.BaseResponse;
import com.hbpu.smartpicture.common.ResultUtils;
import com.hbpu.smartpicture.constant.UserConstant;
import com.hbpu.smartpicture.exception.BusinessException;
import com.hbpu.smartpicture.exception.ErrorCode;
import com.hbpu.smartpicture.exception.ThrowUtils;
import com.hbpu.smartpicture.manager.CosManager;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.websocket.server.PathParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

/**
 * 腾讯云对象存储文件接口
 */
@Tag(name = "腾讯云对象存储文件接口")
@RestController
@RequestMapping("/cosfile")
@Slf4j
public class CosFileController {
    private final CosManager cosManager;

    public CosFileController(CosManager cosManager) {
        this.cosManager = cosManager;
    }

    /**
     * 【测试\管理员】向对象存储上传文件
     *
     * @param file 目标文件
     * @return 返回上传结果
     */
    @Operation(summary = "【测试\\管理员】向对象存储上传文件", description = "【测试\\管理员】向对象存储上传文件")
    @PostMapping("/test/put")
    @AuthCheck(mustRole = UserConstant.ROLE_ADMIN)
    public BaseResponse<String> putObject(@Parameter(description = "前端要传输的文件", required = true) @RequestPart("file") MultipartFile file) {
        String originalKey = file.getOriginalFilename();
        ThrowUtils.throwIf(
                originalKey == null,
                new BusinessException(ErrorCode.PARAMS_ERROR, "文件名为空")
        );
        try {
            String finalUploadKey = String.format("/test/%s", originalKey);
            cosManager.putObject(finalUploadKey, file);
            return ResultUtils.success("上传成功！");
        } catch (Exception ex) {
            log.error("上传失败：{}", ex.getMessage(), ex);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }
    }

    /**
     * 从对象存储下载一个对象
     *
     * @param key 目标对象路径名称
     * @param response 用户响应
     */
    @Parameter(name = "key", description = "目标对象路径名称", in = ParameterIn.PATH, required = true)
    @Operation(summary = "从对象存储下载一个对象", description = "从对象存储下载一个对象")
    @GetMapping("/test/get")
    @AuthCheck(mustRole = UserConstant.ROLE_ADMIN)
    public void getObject(@RequestParam String key, HttpServletResponse response) {
        //判断请求参数
        ThrowUtils.throwIf(StrUtil.isBlank(key), new BusinessException(ErrorCode.PARAMS_ERROR));
        String finalDownloadKey = String.format("/test/%s", key);
        try (COSObject object = cosManager.getObject(finalDownloadKey);
             COSObjectInputStream objectContent = object.getObjectContent()) {
            // 提取文件名
            String fileName = Paths.get(object.getKey()).getFileName().toString();
            // 设置响应头
            response.setContentType("application/octet-stream");
            //处理响应头中的文件名中的中文问题，保证能正常传输
            response.setHeader(
                    HttpHeaders.CONTENT_DISPOSITION,
                    ContentDisposition.attachment()
                                      .filename(fileName, StandardCharsets.UTF_8)
                                      .build()
                                      .toString()
            );
            // 使用 Spring 的工具类更简洁
            //将输入流放入到响应信息中的输出流中，结束后会自动调用flush
            StreamUtils.copy(objectContent, response.getOutputStream());
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败！");
        }
    }
}

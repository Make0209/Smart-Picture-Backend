package com.hbpu.smartpicture.controller;

import cn.hutool.core.util.StrUtil;
import com.hbpu.smartpicture.annotation.AuthCheck;
import com.hbpu.smartpicture.common.BaseResponse;
import com.hbpu.smartpicture.common.ResultUtils;
import com.hbpu.smartpicture.constant.UserConstant;
import com.hbpu.smartpicture.exception.BusinessException;
import com.hbpu.smartpicture.exception.ErrorCode;
import com.hbpu.smartpicture.exception.ThrowUtils;
import com.hbpu.smartpicture.manager.ObsManager;
import com.obs.services.model.ObsObject;
import com.obs.services.model.PutObjectResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 控制文件上传的接口
 */
@Tag(name = "控制文件上传的接口", description = "控制文件上传的接口")
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {
    private final ObsManager obsManager;

    public FileController(ObsManager obsManager) {
        this.obsManager = obsManager;
    }

    /**
     * 上传文件
     *
     * @param file 前端要传输的文件
     * @return 返回上传成功后的访问文件的url
     */
    //@Parameter(name = "file", description = "前端要传输的文件", in = ParameterIn.QUERY, required = true)
    @Operation(summary = "上传文件", description = "上传文件")
    @AuthCheck(mustRole = UserConstant.ROLE_ADMIN)
    @PostMapping("/put")
    public BaseResponse<String> putFile(@Parameter(description = "前端要传输的文件", required = true) @RequestPart("file") MultipartFile file) {


        try (InputStream inputStream = file.getInputStream()) {
            obsManager.putObject(file.getOriginalFilename(), inputStream);
            return ResultUtils.success("上传成功！");
        } catch (IOException e) {
            log.error("文件上传时出现错误：{}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传文件时出现错误！");
        }
    }

    /**
     * 下载文件
     *
     * @param objectKey 目标文件名称
     * @param response  用户响应
     */
    @Parameter(name = "objectKey", description = "目标文件名称", in = ParameterIn.PATH, required = true)
    @Operation(summary = "下载文件", description = "下载文件")
    @GetMapping("/get/{objectKey}")
    public void getFile(@PathVariable String objectKey, HttpServletResponse response) {
        //判断参数是否错误
        ThrowUtils.throwIf(StrUtil.isBlank(objectKey), new BusinessException(ErrorCode.PARAMS_ERROR));
        //从OBS下载文件
        ObsObject obsObject = obsManager.getObject(objectKey);
        //获取文件内容的输入流
        try (InputStream inputStream = obsObject.getObjectContent()) {
            // 设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            //处理响应头中的文件名中的中文问题，保证能正常传输
            response.setHeader(
                    HttpHeaders.CONTENT_DISPOSITION,
                    ContentDisposition.attachment()
                                      .filename(obsObject.getObjectKey(), StandardCharsets.UTF_8)
                                      .build()
                                      .toString()
            );
            // 使用 Spring 的工具类更简洁
            //将输入流放入到响应信息中的输出流中，结束后会自动调用flush
            StreamUtils.copy(inputStream, response.getOutputStream());
        } catch (IOException e) {
            log.error("下载文件时出现异常:{}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败!");
        }
    }

}

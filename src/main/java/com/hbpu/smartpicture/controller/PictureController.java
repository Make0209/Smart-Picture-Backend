package com.hbpu.smartpicture.controller;

import com.hbpu.smartpicture.annotation.AuthCheck;
import com.hbpu.smartpicture.common.BaseResponse;
import com.hbpu.smartpicture.common.ResultUtils;
import com.hbpu.smartpicture.constant.UserConstant;
import com.hbpu.smartpicture.exception.ErrorCode;
import com.hbpu.smartpicture.exception.ThrowUtils;
import com.hbpu.smartpicture.model.dto.picture.PictureUploadDTO;
import com.hbpu.smartpicture.model.vo.picture.PictureVO;
import com.hbpu.smartpicture.service.PictureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图片功能接口
 */
@Tag(name = "图片功能接口", description = "图片功能接口")
@RestController
@RequestMapping("/picture")
public class PictureController {
    private final PictureService pictureService;

    public PictureController(PictureService pictureService) {
        this.pictureService = pictureService;
    }

    /**
     * 【管理员】上传图片
     *
     * @param file    目标文件
     * @param id      图片id
     * @param request 用户请求
     * @return 图片信息封装类
     */
    @Operation(summary = "【管理员】上传图片", description = "【管理员】上传图片")
    @AuthCheck(mustRole = UserConstant.ROLE_ADMIN)
    @PostMapping("/upload")
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile file, @RequestParam(value = "id", required = false) Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(file.isEmpty(), ErrorCode.PARAMS_ERROR);
        PictureUploadDTO pictureUploadDTO = new PictureUploadDTO();
        if (id != null) {
            pictureUploadDTO.setId(id);
        }
        PictureVO pictureVO = pictureService.uploadPicture(file, pictureUploadDTO, request);
        return ResultUtils.success(pictureVO);
    }
}

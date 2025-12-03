package com.hbpu.smartpicture.manager;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.hbpu.smartpicture.exception.ErrorCode;
import com.hbpu.smartpicture.exception.ThrowUtils;
import com.hbpu.smartpicture.model.dto.file.UploadPictureResultDTO;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * 操作对象存储的详细实现类
 */
@Component
public class FileManager {
    private final CosManager cosManager;
    private final List<String> SUFFIX_WHITELIST = Arrays.asList("jpg", "jpeg", "png", "webp");

    public FileManager(CosManager cosManager) {
        this.cosManager = cosManager;
    }

    /**
     * 上传图片到对象存储
     *
     * @param file       目标文件
     * @param folderPath 目标文件夹
     * @return 返回图片信息封装类
     */
    public UploadPictureResultDTO uploadPicture(MultipartFile file, String folderPath) {
        // 进行文件校验
        validPicture(file);
        // 创建图片上传地址
        String uuid = RandomUtil.randomString(16);
        String fileName = LocalDate.now() + uuid + "." + FileUtil.getSuffix(file.getOriginalFilename());
        String uploadFileKey = String.format("/%s/%s", folderPath, fileName);
        //上传图片
        PutObjectResult putObjectResult = cosManager.putPicture(uploadFileKey, file);
        // 根据上传结果获取图片信息
        ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
        int width = imageInfo.getWidth();
        int height = imageInfo.getHeight();
        double picScale = NumberUtil.round(width * 1.0 / height, 2).doubleValue();
        // 将图片信息封装进返回类中
        UploadPictureResultDTO uploadPictureResultDTO = new UploadPictureResultDTO();
        uploadPictureResultDTO.setUrl("https://" + putObjectResult.getCiUploadResult().getOriginalInfo().getLocation());
        uploadPictureResultDTO.setName(fileName);
        uploadPictureResultDTO.setPicSize(file.getSize());
        uploadPictureResultDTO.setPicWidth(width);
        uploadPictureResultDTO.setPicHeight(height);
        uploadPictureResultDTO.setPicScale(picScale);
        uploadPictureResultDTO.setPicFormat(imageInfo.getFormat());
        // 返回结果封装类
        return uploadPictureResultDTO;
    }

    /**
     * 校验文件大小和类型
     *
     * @param file 目标文件
     */
    private void validPicture(MultipartFile file) {
        // 判断文件是否为空
        ThrowUtils.throwIf(
                file == null,
                ErrorCode.PARAMS_ERROR,
                "文件不能为空！"
        );
        // 检查文件大小
        long fileSize = file.getSize();
        final long MAX_FILE_SIZE = 1024 * 1024;
        ThrowUtils.throwIf(
                fileSize > MAX_FILE_SIZE,
                ErrorCode.PARAMS_ERROR,
                "文件大小不能超过2MB！"
        );
        // 检查文件类型
        String suffix = FileUtil.getSuffix(file.getOriginalFilename());
        ThrowUtils.throwIf(
                !SUFFIX_WHITELIST.contains(suffix),
                ErrorCode.PARAMS_ERROR,
                "不支持的文件类型！"
        );
    }

}

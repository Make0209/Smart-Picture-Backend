package com.hbpu.smartpicture.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.hbpu.smartpicture.manager.CosManager;
import com.hbpu.smartpicture.model.dto.file.UploadPictureResultDTO;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDate;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 上传图片模板类
 */
@Slf4j
@Component
public abstract class PictureUploadTemplate {
    private final CosManager cosManager;


    public PictureUploadTemplate(CosManager cosManager) {
        this.cosManager = cosManager;
    }

    /**
     * 上传图片到对象存储
     *
     * @param inputSource 目标文件
     * @param folderPath  目标文件夹
     * @return 返回图片信息封装类
     */
    public UploadPictureResultDTO uploadPicture(Object inputSource, String folderPath) {
        // 进行文件校验
        validPicture(inputSource);
        // 创建图片上传地址
        String uuid = RandomUtil.randomString(16);
        String fileName = LocalDate.now() + uuid + "." + FileUtil.getSuffix(getOriginalFilename(inputSource));
        String uploadFileKey = String.format("/%s/%s", folderPath, fileName);
        MultipartFile file = processFile(inputSource);
        //上传图片
        PutObjectResult putObjectResult = cosManager.putPicture(uploadFileKey, file);
        // 根据上传结果获取图片信息
        ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
        return bulidResult(imageInfo, putObjectResult, fileName, file);
    }

    /**
     * 封装返回结果
     *
     * @param imageInfo       图片信息
     * @param putObjectResult 上传对象结果
     * @param fileName        文件名
     * @param file            目标文件
     * @return 上传结果封装类
     */
    private static @NonNull UploadPictureResultDTO bulidResult(ImageInfo imageInfo, PutObjectResult putObjectResult, String fileName, MultipartFile file) {
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

    protected abstract String getOriginalFilename(Object inputSource);

    protected abstract MultipartFile processFile(Object inputSource);

    protected abstract void validPicture(Object inputSource);
}


package com.hbpu.smartpicture.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.hbpu.smartpicture.manager.CosManager;
import com.hbpu.smartpicture.model.dto.file.UploadPictureResultDTO;
import com.hbpu.smartpicture.utils.ColorTransformUtils;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDate;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
        String uuid = RandomUtil.randomString(16); // 生成一个随机的UUID
        String fileName = LocalDate.now() + uuid; // 使用当前日期和UUID拼接文件名
        String uploadFileKey = String.format("/%s/%s", folderPath, fileName); // 构造上传路径
        MultipartFile file = processFile(inputSource); // 处理输入源文件
        // 上传图片到COS
        PutObjectResult putObjectResult = cosManager.putPicture(uploadFileKey, file);
        // 获取图片信息和处理结果
        ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo(); // 原始图片信息
        List<CIObject> processedPic = putObjectResult.getCiUploadResult().getProcessResults()
                                                     .getObjectList(); // 处理后的图片列表
        String realFileName = FileUtil.getName(
                putObjectResult.getCiUploadResult().getOriginalInfo().getKey()); // 获取原始文件名

        if (CollUtil.isNotEmpty(processedPic)) {
            CIObject compressedObject = processedPic.get(0); // 压缩图
            // 缩略图默认等于压缩图
            CIObject thumbnailObject = compressedObject;
            // 又生成缩略图，才获取缩略图信息
            if (processedPic.size() > 1) {
                thumbnailObject = processedPic.get(1); // 获取第二个图片作为缩略图
            }
            return bulidResult(compressedObject, realFileName, thumbnailObject, imageInfo); // 构建包含压缩图和缩略图的结果
        }
        return bulidResult(imageInfo, putObjectResult, realFileName, file); // 构建包含原始信息和文件结果的最终返回
    }

    /**
     * 封装返回结果
     * @param compressedObject webp处理后的对象
     * @param fileName 文件名
     * @param thumbnailObject 缩放后的对象
     * @return 返回上传结果封装类
     */
    private UploadPictureResultDTO bulidResult(CIObject compressedObject, String fileName, CIObject thumbnailObject, ImageInfo imageInfo) {
        int width = compressedObject.getWidth(); // 获取压缩后图片的宽度
        int height = compressedObject.getHeight(); // 获取压缩后图片的高度
        double picScale = NumberUtil.round(width * 1.0 / height, 2).doubleValue(); // 计算并保留两位小数的图片宽高比

        // 将图片信息封装进返回类中
        UploadPictureResultDTO uploadPictureResultDTO = new UploadPictureResultDTO();
        uploadPictureResultDTO.setUrl("https://" + compressedObject.getLocation()); // 设置上传后图片的URL
        uploadPictureResultDTO.setName(fileName); // 设置上传后图片的名称
        uploadPictureResultDTO.setPicSize(Long.valueOf(compressedObject.getSize())); // 设置上传后图片的大小
        uploadPictureResultDTO.setPicWidth(width); // 设置上传后图片的宽度
        uploadPictureResultDTO.setPicHeight(height); // 设置上传后图片的高度
        uploadPictureResultDTO.setPicScale(picScale); // 设置上传后图片的宽高比
        uploadPictureResultDTO.setPicFormat(compressedObject.getFormat()); // 设置上传后图片的格式
        uploadPictureResultDTO.setThumbnailUrl("https://" + thumbnailObject.getLocation()); // 设置缩略图的URL
        // 获取颜色值
        String picColor = ColorTransformUtils.colorTransform(imageInfo.getAve());
        // 设置图片的平均颜色
        uploadPictureResultDTO.setPicColor(picColor);

        // 返回结果封装类
        return uploadPictureResultDTO;
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
        int width = imageInfo.getWidth(); // 获取图片宽度
        int height = imageInfo.getHeight(); // 获取图片高度
        double picScale = NumberUtil.round(width * 1.0 / height, 2).doubleValue(); // 计算并四舍五入得到图片的宽高比

        // 将图片信息封装进返回类中
        UploadPictureResultDTO uploadPictureResultDTO = new UploadPictureResultDTO();
        uploadPictureResultDTO.setUrl(
                "https://" + putObjectResult.getCiUploadResult().getOriginalInfo().getLocation()); // 设置上传后的图片URL
        uploadPictureResultDTO.setName(fileName); // 设置图片名称
        uploadPictureResultDTO.setPicSize(file.getSize()); // 设置图片大小
        uploadPictureResultDTO.setPicWidth(width); // 设置图片宽度
        uploadPictureResultDTO.setPicHeight(height); // 设置图片高度
        uploadPictureResultDTO.setPicScale(picScale); // 设置图片宽高比
        uploadPictureResultDTO.setPicFormat(imageInfo.getFormat()); // 设置图片格式
        // 获取颜色值
        String picColor = ColorTransformUtils.colorTransform(imageInfo.getAve());
        // 设置图片的平均颜色
        uploadPictureResultDTO.setPicColor(picColor);

        // 返回结果封装类
        return uploadPictureResultDTO;
    }

    protected abstract String getOriginalFilename(Object inputSource);

    protected abstract MultipartFile processFile(Object inputSource);

    protected abstract void validPicture(Object inputSource);
}


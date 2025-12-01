package com.hbpu.smartpicture.manager;


import com.hbpu.smartpicture.exception.BusinessException;
import com.hbpu.smartpicture.exception.ErrorCode;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Component
public class CosManager {

    private final COSClient cosClient;
    @Value("${tencent.cos.bucketName}")
    private String bucket;

    public CosManager(COSClient cosClient) {
        this.cosClient = cosClient;
    }

    /**
     * 上传一个对象到COS
     *
     * @param key  目标对象路径名称
     * @param file 目标文件
     * @return 返回一个上传结果封装类
     */
    public PutObjectResult putObject(String key, MultipartFile file) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest req = new PutObjectRequest(bucket, key, inputStream, metadata);
            return cosClient.putObject(req);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件读取失败！");
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败！");
        }
    }

    /**
     * 下载一个对象
     * @param key 目标对象的路径名称
     * @return 返回一个下载信息封装类
     */
    public COSObject getObject(String key) {
        try {
            return cosClient.getObject(bucket, key);
        } catch (CosClientException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "未找到指定图片！");
        }
    }
}

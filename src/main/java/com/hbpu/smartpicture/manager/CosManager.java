package com.hbpu.smartpicture.manager;


import cn.hutool.core.io.FileUtil;
import com.hbpu.smartpicture.exception.BusinessException;
import com.hbpu.smartpicture.exception.ErrorCode;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * 上传一个图片到COS并返回基本信息
     *
     * @param key  目标对象路径名称
     * @param file 目标文件
     * @return 返回一个上传结果封装类
     */
    public PutObjectResult putPicture(String key, MultipartFile file) {
        // 设置文件的源信息，包括文件大小和文件类型
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());
        // 获取文件输入流
        try (InputStream inputStream = file.getInputStream()) {
            // 构建完整上传路径
            String webpKey = key + ".webp";
            // 构建上传请求
            PutObjectRequest req = new PutObjectRequest(bucket, webpKey, inputStream, metadata);
            // 构建图片处理规则
            PicOperations picOperations = new PicOperations();
            picOperations.setIsPicInfo(1);
            // 创建处理规则列表
            List<PicOperations.Rule> rules = new ArrayList<>();
            PicOperations.Rule rule = new PicOperations.Rule();
            rule.setFileId(FileUtil.getName(webpKey));
            rule.setRule("imageMogr2/format/webp");
            rule.setBucket(bucket);
            // 将处理规则加入列表
            rules.add(rule);
            // 放入图片处理选项类中
            picOperations.setRules(rules);
            // 放入上传请求类中
            req.setPicOperations(picOperations);
            return cosClient.putObject(req);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件读取失败！");
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败！");
        }
    }
}

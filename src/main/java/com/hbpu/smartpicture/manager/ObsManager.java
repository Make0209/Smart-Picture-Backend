package com.hbpu.smartpicture.manager;

import com.obs.services.ObsClient;
import com.obs.services.model.ObsObject;
import com.obs.services.model.PutObjectResult;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * 操作OBS存储对象的类
 */
@Component
public class ObsManager {

    private final ObsClient obsClient;
    private final String BUCKET = "kefan-picture";

    public ObsManager(ObsClient obsClient) {
        this.obsClient = obsClient;
    }

    /**
     * 向OBS上传文件
     * @param objectKey 文件名
     * @param inputStream 文件输入流
     * @return 返回上传结果信息封装类
     */
    public PutObjectResult putObject(String objectKey, InputStream inputStream) {
        return obsClient.putObject(BUCKET, objectKey, inputStream);
    }

    /**
     * 从OBS下载文件
     * @param objectKey 目标文件名称
     * @return 返回下载结果信息封装类
     */
    public ObsObject getObject(String objectKey) {
        return obsClient.getObject(BUCKET, objectKey);
    }
}

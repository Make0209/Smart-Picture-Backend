package com.hbpu.smartpicture.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.hbpu.smartpicture.exception.BusinessException;
import com.hbpu.smartpicture.exception.ErrorCode;
import com.hbpu.smartpicture.exception.ThrowUtils;
import com.hbpu.smartpicture.manager.CosManager;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * 文件图片上传
 */
@Service
public class FilePictureUpload extends PictureUploadTemplate {
    private final List<String> SUFFIX_WHITELIST = Arrays.asList("jpg", "jpeg", "png", "webp", "PNG");
    public FilePictureUpload(CosManager cosManager) {
        super(cosManager);
    }

    /**
     * 获取原文件名
     * @param inputSource 文件源
     * @return 返回文件名
     */
    @Override
    protected String getOriginalFilename(Object inputSource) {
        MultipartFile file = processFile(inputSource);
        return file.getOriginalFilename();
    }

    /**
     * 处理文件源
     * @param inputSource 文件源
     * @return 返回MultipartFile
     */
    @Override
    protected MultipartFile processFile(Object inputSource) {
        if (inputSource instanceof MultipartFile) {
            return (MultipartFile) inputSource;
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
    }

    /**
     * 校验文件大小和类型
     *
     * @param inputSource 目标文件源
     */
    @Override
    protected void validPicture(Object inputSource) {
        MultipartFile file = processFile(inputSource);
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

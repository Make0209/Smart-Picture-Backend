package com.hbpu.smartpicture.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hbpu.smartpicture.model.dto.picture.PictureQueryDTO;
import com.hbpu.smartpicture.model.dto.picture.PictureReviewDTO;
import com.hbpu.smartpicture.model.dto.picture.PictureUploadByBatchDTO;
import com.hbpu.smartpicture.model.dto.picture.PictureUploadDTO;
import com.hbpu.smartpicture.model.pojo.Picture;
import com.hbpu.smartpicture.model.pojo.User;
import com.hbpu.smartpicture.model.vo.picture.PictureVO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author 马可
 * &#064;description  针对表【picture(图片)】的数据库操作Service
 * &#064;createDate  2025-11-30 22:18:55
 */
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片接口
     *
     * @param inputSource      目标文件源
     * @param uploadDTO 上传信息封装类
     * @param request   用户请求
     * @return 图片信息封装类
     */
    PictureVO uploadPicture(Object inputSource, PictureUploadDTO uploadDTO, HttpServletRequest request);

    /**
     * 获取查询语句接口
     *
     * @param pictureQueryDTO 查询条件封装类
     * @return 返回构建完成的查询语句
     */
    LambdaQueryWrapper<Picture> getQueryWrapper(PictureQueryDTO pictureQueryDTO);


    /**
     * 获取图片信息封装类
     *
     * @param picture 目标对象
     * @param request 用户请求
     * @return 返回图片信息封装类VO
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 分页获取图片信息封装类接口
     * @param picturePage 图片分页对象
     * @return PictureVO类型的图片分页对象
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage);

    /**
     * 检验图片接口
     * @param picture 目标图片
     */
    void validPicture(Picture picture);

    /**
     * 图片审核接口
     *
     * @param pictureReviewDTO 图片审核信息封装类
     * @param request          用户请求
     */
    void pictureReview(PictureReviewDTO pictureReviewDTO, HttpServletRequest request);

    /**
     * 重置审核状态并自动过审接口
     *
     * @param picture   目标图片
     * @param loginUser 当前用户
     */
    void resetReviewStatus(Picture picture, User loginUser);

    /**
     * 批量上传图片
     *
     * @param pictureUploadByBatchDTO 批量上传图片请求封装类
     * @param request                 用户请求
     * @return 成功上传图片的数量
     */
    Integer uploadPictureByBatch(PictureUploadByBatchDTO pictureUploadByBatchDTO, HttpServletRequest request);

    /**
     * 删除对象存储中的图片
     * @param picture 目标图片
     */
    void clearPicture(Picture picture);
}

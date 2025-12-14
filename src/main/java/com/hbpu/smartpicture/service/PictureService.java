package com.hbpu.smartpicture.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hbpu.smartpicture.model.dto.picture.PictureQueryDTO;
import com.hbpu.smartpicture.model.dto.picture.PictureUploadDTO;
import com.hbpu.smartpicture.model.pojo.Picture;
import com.hbpu.smartpicture.model.vo.picture.PictureVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author 马可
 * &#064;description  针对表【picture(图片)】的数据库操作Service
 * &#064;createDate  2025-11-30 22:18:55
 */
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片接口
     *
     * @param file      目标文件
     * @param uploadDTO 上传信息封装类
     * @param request   用户请求
     * @return 图片信息封装类
     */
    PictureVO uploadPicture(MultipartFile file, PictureUploadDTO uploadDTO, HttpServletRequest request);

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
}

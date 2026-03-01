package com.hbpu.smartpicture.model.vo.picture;

import cn.hutool.json.JSONUtil;
import com.hbpu.smartpicture.model.pojo.Picture;
import com.hbpu.smartpicture.model.vo.user.UserVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 返回给前端的图片信息的封装类
 */
@Schema(description = "返回给前端的图片信息的封装类")
@Data
public class PictureVO implements Serializable {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = -1791685503496287343L;

    /**
     * id
     */
    @Schema(description = "id")
    private Long id;

    /**
     * 图片 url
     */
    @Schema(description = "图片 url")
    private String url;

    /**
     * 图片名称
     */
    @Schema(description = "图片名称")
    private String name;

    /**
     * 简介
     */
    @Schema(description = "简介")
    private String introduction;

    /**
     * 标签
     */
    @Schema(description = "标签")
    private List<String> tags;

    /**
     * 分类
     */
    @Schema(description = "分类")
    private String category;

    /**
     * 文件体积
     */
    @Schema(description = "文件体积")
    private Long picSize;

    /**
     * 图片宽度
     */
    @Schema(description = "图片宽度")
    private Integer picWidth;

    /**
     * 图片高度
     */
    @Schema(description = "图片高度")
    private Integer picHeight;

    /**
     * 图片比例
     */
    @Schema(description = "图片比例")
    private Double picScale;

    /**
     * 图片格式
     */
    @Schema(description = "图片格式")
    private String picFormat;

    /**
     * 用户 id
     */
    @Schema(description = "用户 id")
    private Long userId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 编辑时间
     */
    @Schema(description = "编辑时间")
    private LocalDateTime editTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 创建用户信息
     */
    @Schema(description = "创建用户信息")
    private UserVO user;

    /**
     * 缩略图url
     */
    @Schema(description = "缩略图url")
    private String thumbnailUrl;

    /**
     * 图片主色调
     */
    @Schema(description = "图片主色调")
    private String picColor;

    /**
     * 空间 id
     */
    @Schema(description = "空间 id")
    private Long spaceId;

    /**
     * 权限列表
     */
    @Schema(description = "权限列表")
    private List<String> permissionList = new ArrayList<>();


    /**
     * 封装类转对象
     */
    public static Picture voToObj(PictureVO pictureVO) {
        if (pictureVO == null) {
            return null;
        }
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureVO, picture);
        // 类型不同，需要转换
        picture.setTags(JSONUtil.toJsonStr(pictureVO.getTags()));
        return picture;
    }

    /**
     * 对象转封装类
     */
    public static PictureVO objToVo(Picture picture) {
        if (picture == null) {
            return null;
        }
        PictureVO pictureVO = new PictureVO();
        BeanUtils.copyProperties(picture, pictureVO);
        // 类型不同，需要转换
        pictureVO.setTags(JSONUtil.toList(picture.getTags(), String.class));
        return pictureVO;
    }
}

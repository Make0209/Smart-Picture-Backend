package com.hbpu.smartpicture.model.vo.space;

import com.hbpu.smartpicture.model.pojo.Space;
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
 * 返回给前端的Space封装类
 */
@Schema(description = "返回给前端的Space封装类")
@Data
public class SpaceVO implements Serializable {
    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = 6491981444272469256L;

    /**
     * id
     */
    @Schema(description = "id")
    private Long id;

    /**
     * 空间名称
     */
    @Schema(description = "空间名称")
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    @Schema(description = "空间级别：0-普通版 1-专业版 2-旗舰版")
    private Integer spaceLevel;

    /**
     * 空间图片的最大总大小
     */
    @Schema(description = "空间图片的最大总大小")
    private Long maxSize;

    /**
     * 空间图片的最大数量
     */
    @Schema(description = "空间图片的最大数量")
    private Long maxCount;

    /**
     * 当前空间下图片的总大小
     */
    @Schema(description = "当前空间下图片的总大小")
    private Long totalSize;

    /**
     * 当前空间下的图片数量
     */
    @Schema(description = "当前空间下的图片数量")
    private Long totalCount;

    /**
     * 创建用户 id
     */
    @Schema(description = "创建用户 id")
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
     * 空间类型：0-私有 1-团队
     */
    @Schema(description = "空间类型：0-私有 1-团队")
    private Integer spaceType;

    /**
     * 权限列表
     */
    @Schema(description = "权限列表")
    private List<String> permissionList = new ArrayList<>();



    /**
     * 封装类转对象
     *
     * @param spaceVO 目标Space对象
     * @return VO对象
     */
    public static Space voToObj(SpaceVO spaceVO) {
        if (spaceVO == null) {
            return null;
        }
        Space space = new Space();
        BeanUtils.copyProperties(spaceVO, space);
        return space;
    }

    /**
     * 对象转封装类
     *
     * @param space 目标Space对象
     * @return 原Space对象
     */
    public static SpaceVO objToVo(Space space) {
        if (space == null) {
            return null;
        }
        SpaceVO spaceVO = new SpaceVO();
        BeanUtils.copyProperties(space, spaceVO);
        return spaceVO;
    }
}


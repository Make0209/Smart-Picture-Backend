package com.hbpu.smartpicture.model.vo.spaceuser;

import com.hbpu.smartpicture.model.pojo.SpaceUser;
import com.hbpu.smartpicture.model.vo.space.SpaceVO;
import com.hbpu.smartpicture.model.vo.user.UserVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 空间用户信息相应信息封装类
 */
@Schema(description = "空间用户信息相应信息封装类")
@Data
public class SpaceUserVO implements Serializable {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = 2701496773613218381L;

    /**
     * id
     */
    @Schema(description = "id")
    private Long id;

    /**
     * 空间 id
     */
    @Schema(description = "空间 id")
    private Long spaceId;

    /**
     * 用户 id
     */
    @Schema(description = "用户 id")
    private Long userId;

    /**
     * 空间角色：viewer/editor/admin
     */
    @Schema(description = "空间角色：viewer/editor/admin")
    private String spaceRole;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 用户信息
     */
    @Schema(description = "用户信息")
    private UserVO user;

    /**
     * 空间信息
     */
    @Schema(description = "空间信息")
    private SpaceVO space;


    /**
     * 封装类转对象
     *
     * @param spaceUserVO 空间用户信息封装类
     * @return 空间用户信息
     */
    public static SpaceUser voToObj(SpaceUserVO spaceUserVO) {
        if (spaceUserVO == null) {
            return null;
        }
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserVO, spaceUser);
        return spaceUser;
    }

    /**
     * 对象转封装类
     *
     * @param spaceUser 空间用户信息
     * @return 空间用户信息封装类
     */
    public static SpaceUserVO objToVo(SpaceUser spaceUser) {
        if (spaceUser == null) {
            return null;
        }
        SpaceUserVO spaceUserVO = new SpaceUserVO();
        BeanUtils.copyProperties(spaceUser, spaceUserVO);
        return spaceUserVO;
    }
}


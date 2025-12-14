package com.hbpu.smartpicture.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbpu.smartpicture.exception.ErrorCode;
import com.hbpu.smartpicture.exception.ThrowUtils;
import com.hbpu.smartpicture.manager.FileManager;
import com.hbpu.smartpicture.mapper.PictureMapper;
import com.hbpu.smartpicture.model.dto.file.UploadPictureResultDTO;
import com.hbpu.smartpicture.model.dto.picture.PictureQueryDTO;
import com.hbpu.smartpicture.model.dto.picture.PictureUploadDTO;
import com.hbpu.smartpicture.model.pojo.Picture;
import com.hbpu.smartpicture.model.pojo.User;
import com.hbpu.smartpicture.model.vo.picture.PictureVO;
import com.hbpu.smartpicture.model.vo.user.UserVO;
import com.hbpu.smartpicture.service.PictureService;
import com.hbpu.smartpicture.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 马可
 * &#064;description  针对表【picture(图片)】的数据库操作Service实现
 * &#064;createDate  2025-11-30 22:18:55
 */
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

    private final FileManager fileManager;
    private final UserService userService;

    public PictureServiceImpl(FileManager fileManager, UserService userService) {
        this.fileManager = fileManager;
        this.userService = userService;
    }

    /**
     * 上传一张图片
     *
     * @param file      目标文件
     * @param uploadDTO 上传图片请求封装类
     * @param request   用户请求
     * @return 返回图片信息封装类
     */
    @Override
    public PictureVO uploadPicture(MultipartFile file, PictureUploadDTO uploadDTO, HttpServletRequest request) {
        // 判断是新增还是更新
        Long pictureId = null;
        if (uploadDTO != null) {
            pictureId = uploadDTO.getId();
        }
        // 如果是更新，先查询数据库中有无该图片，因为先得有才能更新
        if (pictureId != null) {
            boolean exists = this.lambdaQuery().eq(Picture::getId, pictureId).exists();
            ThrowUtils.throwIf(!exists, ErrorCode.PARAMS_ERROR, "要上传的图片不存在");
        }
        // 上传图片
        User currentUser = userService.getCurrentUser(request);
        String folderPath = String.format("public/%s", currentUser.getId());
        UploadPictureResultDTO uploadPictureResultDTO = fileManager.uploadPicture(file, folderPath);
        //操作数据库
        Picture picture = new Picture();
        BeanUtils.copyProperties(uploadPictureResultDTO, picture);
        picture.setUserId(currentUser.getId());
        if (pictureId != null) {
            picture.setId(pictureId);
        }
        // 根据id是否有值来判断更新还是插入
        boolean savaResult = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!savaResult, ErrorCode.OPERATION_ERROR, "上传失败！");
        Picture newPicture = this.getById(picture.getId());
        // 将VO对象返回
        PictureVO pictureVO = PictureVO.objToVo(newPicture);
        pictureVO.setUser(userService.getUserVO(currentUser));
        return pictureVO;
    }

    /**
     * 根据查询条件封装类生成查询条件
     *
     * @param pictureQueryDTO 查询条件封装类
     * @return 返回构建好的查询语句
     */
    @Override
    public LambdaQueryWrapper<Picture> getQueryWrapper(PictureQueryDTO pictureQueryDTO) {
        LambdaQueryWrapper<Picture> queryWrapper = new LambdaQueryWrapper<>();
        if (pictureQueryDTO == null) {
            return queryWrapper;
        }
        // 获取封装类中的所有属性值
        Long id = pictureQueryDTO.getId();
        String name = pictureQueryDTO.getName();
        String introduction = pictureQueryDTO.getIntroduction();
        String category = pictureQueryDTO.getCategory();
        List<String> tags = pictureQueryDTO.getTags();
        Long picSize = pictureQueryDTO.getPicSize();
        Integer picWidth = pictureQueryDTO.getPicWidth();
        Integer picHeight = pictureQueryDTO.getPicHeight();
        Double picScale = pictureQueryDTO.getPicScale();
        String picFormat = pictureQueryDTO.getPicFormat();
        String searchText = pictureQueryDTO.getSearchText();
        Long userId = pictureQueryDTO.getUserId();
        String sortField = pictureQueryDTO.getSortField();
        String sortOrder = pictureQueryDTO.getSortOrder();
        // 现根据搜索信息来匹配
        if (StrUtil.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw
                    .like(Picture::getName, searchText)
                    .or()
                    .like(Picture::getIntroduction, searchText)
            );
        }
        // 根据属性值来生成查询条件
        queryWrapper.eq(ObjUtil.isNotEmpty(id), Picture::getId, id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), Picture::getUserId, userId);
        queryWrapper.like(StrUtil.isNotBlank(name), Picture::getName, name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), Picture::getIntroduction, introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), Picture::getPicFormat, picFormat);
        queryWrapper.eq(StrUtil.isNotBlank(category), Picture::getCategory, category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), Picture::getPicWidth, picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), Picture::getPicHeight, picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), Picture::getPicSize, picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), Picture::getPicScale, picScale);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like(Picture::getTags, "\"" + tag + "\"");
            }
        }
        // 5. 排序处理（防止 SQL 注入）
        //拼接字符串
        if (StrUtil.isNotBlank(sortField)) {
            String orderSql = "ORDER BY " + sortField + " " + ("ascend".equals(sortOrder) ? "ASC" : "DESC");
            queryWrapper.last(orderSql);
        }
        return queryWrapper;
    }

    /**
     * 获取图片信息封装类
     *
     * @param picture 目标对象
     * @param request 用户请求
     * @return 返回VO对象
     */
    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        PictureVO pictureVO = PictureVO.objToVo(picture);
        if (picture.getUserId() != null && picture.getUserId() > 0) {
            UserVO userVO = userService.getUserVO(userService.getCurrentUser(request));
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }


    /**
     * 获取图片信息封装类（分页）
     *
     * @param picturePage 图片分页对象
     * @return 返回PictureVO的分页封装类
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(
                picturePage.getCurrent(),
                picturePage.getSize(),
                picturePage.getTotal()
        );
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
        // 1. 关联查询用户信息
        // 使用set集合去重
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        // 使用Map集合来进行存储，使用用户id作为key，User对象作为value，
        // 在下面进行使用的时候也不用循环遍历，而是直接根据用户id作为key直接拿到user对象，降低时间复杂度
        Map<Long, User> userIdUserListMap = userService.listByIds(userIdSet).stream()
                                                       .collect(Collectors.toMap(User::getId, user -> user));
        // 2. 填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId);
            }
            pictureVO.setUser(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }


    /**
     * 校验图片信息
     *
     * @param picture 目标图片对象
     */
    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

}





package com.hbpu.smartpicture.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.hbpu.smartpicture.annotation.AuthCheck;
import com.hbpu.smartpicture.common.BaseResponse;
import com.hbpu.smartpicture.common.DeleteRequest;
import com.hbpu.smartpicture.common.ResultUtils;
import com.hbpu.smartpicture.constant.UserConstant;
import com.hbpu.smartpicture.exception.BusinessException;
import com.hbpu.smartpicture.exception.ErrorCode;
import com.hbpu.smartpicture.exception.ThrowUtils;
import com.hbpu.smartpicture.model.dto.picture.*;
import com.hbpu.smartpicture.model.enums.PictureReviewStatusEnum;
import com.hbpu.smartpicture.model.pojo.Picture;
import com.hbpu.smartpicture.model.pojo.Space;
import com.hbpu.smartpicture.model.pojo.User;
import com.hbpu.smartpicture.model.vo.picture.PictureTagCategoryVO;
import com.hbpu.smartpicture.model.vo.picture.PictureVO;
import com.hbpu.smartpicture.service.PictureService;
import com.hbpu.smartpicture.service.SpaceService;
import com.hbpu.smartpicture.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * 图片功能接口
 */
@Tag(name = "PictureController", description = "图片功能接口")
@RestController
@RequestMapping("/picture")
public class PictureController {
    private final PictureService pictureService;
    private final UserService userService;
    private final SpaceService spaceService;
    private final StringRedisTemplate stringRedisTemplate;

    private final Cache<String, String> LOCAL_CACHE = Caffeine.newBuilder()
                                                              .initialCapacity(1024) //初始容量
                                                              .maximumSize(10_000) // 最大存储数量（10000条）
                                                              .expireAfterWrite(Duration.ofMinutes(5)) // 缓存过期时间（5分钟）
                                                              .build();

    public PictureController(PictureService pictureService, UserService userService, SpaceService spaceService, StringRedisTemplate stringRedisTemplate) {
        this.pictureService = pictureService;
        this.userService = userService;
        this.spaceService = spaceService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 上传图片
     *
     * @param file    目标文件
     * @param id      图片id
     * @param request 用户请求
     * @return 图片信息封装类
     */
    @Operation(summary = "上传图片", description = "上传图片")
    @AuthCheck(mustRole = UserConstant.ROLE_USER)
    @PostMapping("/upload")
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile file, @RequestParam(value = "id", required = false) Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(file.isEmpty(), ErrorCode.PARAMS_ERROR);
        PictureUploadDTO pictureUploadDTO = new PictureUploadDTO();
        if (id != null) {
            pictureUploadDTO.setId(id);
        }
        PictureVO pictureVO = pictureService.uploadPicture(file, pictureUploadDTO, request);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 根据Url地址上传图片
     *
     * @param pictureUploadDTO 图片上传信息封装类
     * @param request          用户请求
     * @return 图片信息封装类
     */
    @Operation(summary = "根据Url地址上传图片", description = "根据Url地址上传图片")
    @AuthCheck(mustRole = UserConstant.ROLE_USER)
    @PostMapping("/upload/url")
    public BaseResponse<PictureVO> uploadPictureByUrl(@RequestBody PictureUploadDTO pictureUploadDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUploadDTO == null, ErrorCode.PARAMS_ERROR);
        PictureVO pictureVO = pictureService.uploadPicture(pictureUploadDTO.getUrl(), pictureUploadDTO, request);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 批量上传图片
     *
     * @param pictureUploadByBatchDTO 批量上传图片请求封装类
     * @param request                 用户请求
     * @return 成功上传图片数量
     */
    @Operation(summary = "批量上传图片", description = "批量上传图片")
    @AuthCheck(mustRole = UserConstant.ROLE_ADMIN)
    @PostMapping("/upload/batch")
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchDTO pictureUploadByBatchDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUploadByBatchDTO == null, ErrorCode.PARAMS_ERROR);
        Integer uploadResult = pictureService.uploadPictureByBatch(pictureUploadByBatchDTO, request);
        return ResultUtils.success(uploadResult);
    }

    /**
     * 删除图片
     *
     * @param deleteRequest 删除请求封装类
     * @param request       用户请求
     * @return 删除成功返回true
     */
    @Operation(summary = "删除图片", description = "删除图片")
    @AuthCheck(mustRole = UserConstant.ROLE_USER)
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        pictureService.deletePicture(deleteRequest, request);
        return ResultUtils.success(true);
    }

    /**
     * 【管理员】更新图片
     *
     * @param pictureUpdateDTO 更新图片信息封装类
     * @param request          用户请求
     * @return 更新成功返回true
     */
    @Operation(summary = "【管理员】更新图片", description = "【管理员】更新图片")
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ROLE_ADMIN)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateDTO pictureUpdateDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(
                pictureUpdateDTO == null || pictureUpdateDTO.getId() <= 0,
                ErrorCode.PARAMS_ERROR
        );
        // 将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateDTO, picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateDTO.getTags()));
        // 数据校验
        pictureService.validPicture(picture);
        // 判断是否存在
        Picture oldPicture = pictureService.getById(pictureUpdateDTO.getId());
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 更新图片审核状态
        pictureService.resetReviewStatus(picture, userService.getCurrentUser(request));
        // 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 【管理员】根据id获取图片
     *
     * @param id 图片id
     * @return 返回图片对象
     */
    @Operation(summary = "【管理员】根据id获取图片", description = "【管理员】根据id获取图片")
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ROLE_ADMIN)
    public BaseResponse<Picture> getPictureById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(picture);
    }

    /**
     * 根据图片获取VO对象
     *
     * @param id      图片id
     * @param request 用户请求
     * @return 返回PictureVO对象
     */
    @Operation(summary = "根据图片获取VO对象", description = "根据图片获取VO对象")
    @AuthCheck(mustRole = UserConstant.ROLE_USER)
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 校验图片权限，只能获取公共图片，无法获取私有图库中的图片
        Long spaceId = picture.getSpaceId();
        if (spaceId != null) {
            pictureService.checkPictureAuth(request, picture);
        }
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVO(picture, request));
    }

    /**
     * 【管理员】分页获取图片
     *
     * @param pictureQueryDTO 分页图片信息封装类
     * @return 返回图片分页对象封装类
     */
    @Operation(summary = "【管理员】分页获取图片", description = "【管理员】分页获取图片")
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ROLE_ADMIN)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryDTO pictureQueryDTO) {
        long current = pictureQueryDTO.getCurrent();
        long size = pictureQueryDTO.getPageSize();
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(
                new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryDTO)
        );
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片VO对象
     *
     * @param pictureQueryDTO 分页图片信息封装类
     * @return 分页图片VO对象封装类
     */
    @Operation(summary = "分页获取图片VO对象", description = "分页获取图片VO对象")
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryDTO pictureQueryDTO, HttpServletRequest request) {
        long current = pictureQueryDTO.getCurrent();
        long size = pictureQueryDTO.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 空间权限校验
        checkSpaceAuth(pictureQueryDTO, request);
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(
                new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryDTO)
        );
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage));
    }

    /**
     * 采用多级缓存分页获取图片VO对象
     *
     * @param pictureQueryDTO 分页图片信息封装类
     * @return 分页图片VO对象封装类
     */
    @Operation(summary = "采用多级缓存分页获取图片VO对象", description = "分页获取图片VO对象")
    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCache(@RequestBody PictureQueryDTO pictureQueryDTO, HttpServletRequest request) {
        long current = pictureQueryDTO.getCurrent();
        long size = pictureQueryDTO.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20 || current <= 0 || size <= 0, ErrorCode.PARAMS_ERROR);
        // 检验空间权限
        checkSpaceAuth(pictureQueryDTO, request);
        // 从缓存中进行查询
        Page<PictureVO> pictureVOPage = pictureService.listPictureVOByPageWithCache(pictureQueryDTO);
        ThrowUtils.throwIf(pictureVOPage == null, ErrorCode.SYSTEM_ERROR);
        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 检验空间权限
     * @param pictureQueryDTO 分页查询封装类
     * @param request 用户请求
     */
    private void checkSpaceAuth(PictureQueryDTO pictureQueryDTO, HttpServletRequest request) {
        // 空间权限校验
        Long spaceId = pictureQueryDTO.getSpaceId();
        // 公开图库
        if (spaceId == null) {
            // 普通用户默认只能查看已过审的公开数据
            pictureQueryDTO.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            pictureQueryDTO.setNullSpaceId(true);
        } else {
            // 私有空间
            User loginUser = userService.getCurrentUser(request);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            if (!loginUser.getId().equals(space.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
            }
        }
    }

    /**
     * 编辑图片
     *
     * @param pictureEditDTO 图片编辑信息封装类
     * @param request        用户请求
     * @return 编辑成功返回true
     */
    @Operation(summary = "编辑图片", description = "编辑图片")
    @AuthCheck(mustRole = UserConstant.ROLE_USER)
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditDTO pictureEditDTO, HttpServletRequest request) {
        if (pictureEditDTO == null || pictureEditDTO.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        pictureService.editPicture(pictureEditDTO, request);
        return ResultUtils.success(true);
    }

    /**
     * 获取标签和分类
     *
     * @return 返回一个标签分类信息的封装类
     */
    @Operation(summary = "获取标签和分类", description = "获取标签和分类")
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategoryVO> listPictureTagCategory() {
        PictureTagCategoryVO pictureTagCategory = new PictureTagCategoryVO();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }

    /**
     * 图片审核接口
     *
     * @param pictureReviewDTO 图片审核请求信息封装类
     * @param request          用户请求
     * @return 操作成功返回true
     */
    @Operation(summary = "图片审核接口", description = "图片审核接口")
    @AuthCheck(mustRole = UserConstant.ROLE_ADMIN)
    @PostMapping("/review")
    public BaseResponse<Boolean> pictureReview(@RequestBody PictureReviewDTO pictureReviewDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewDTO == null, ErrorCode.PARAMS_ERROR);
        pictureService.pictureReview(pictureReviewDTO, request);
        return ResultUtils.success(true);
    }

}

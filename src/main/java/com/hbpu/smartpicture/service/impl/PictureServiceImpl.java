package com.hbpu.smartpicture.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.hbpu.smartpicture.constant.UserConstant;
import com.hbpu.smartpicture.exception.BusinessException;
import com.hbpu.smartpicture.exception.ErrorCode;
import com.hbpu.smartpicture.exception.ThrowUtils;
import com.hbpu.smartpicture.manager.CosManager;
import com.hbpu.smartpicture.manager.upload.FilePictureUpload;
import com.hbpu.smartpicture.manager.upload.PictureUploadTemplate;
import com.hbpu.smartpicture.manager.upload.UrlPictureUpload;
import com.hbpu.smartpicture.mapper.PictureMapper;
import com.hbpu.smartpicture.model.dto.file.UploadPictureResultDTO;
import com.hbpu.smartpicture.model.dto.picture.PictureQueryDTO;
import com.hbpu.smartpicture.model.dto.picture.PictureReviewDTO;
import com.hbpu.smartpicture.model.dto.picture.PictureUploadByBatchDTO;
import com.hbpu.smartpicture.model.dto.picture.PictureUploadDTO;
import com.hbpu.smartpicture.model.enums.PictureReviewStatusEnum;
import com.hbpu.smartpicture.model.pojo.Picture;
import com.hbpu.smartpicture.model.pojo.User;
import com.hbpu.smartpicture.model.vo.picture.PictureVO;
import com.hbpu.smartpicture.model.vo.user.UserVO;
import com.hbpu.smartpicture.service.PictureService;
import com.hbpu.smartpicture.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author 马可
 * &#064;description  针对表【picture(图片)】的数据库操作Service实现
 * &#064;createDate  2025-11-30 22:18:55
 */
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

    private final UserService userService;
    private final FilePictureUpload filePictureUpload;
    private final UrlPictureUpload urlPictureUpload;
    private final CosManager cosManager;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;

    private final Cache<String, String> LOCAL_CACHE = Caffeine.newBuilder()
                                                              .initialCapacity(1024) //初始容量
                                                              .maximumSize(10_000) // 最大存储数量（10000条）
                                                              .expireAfterWrite(Duration.ofMinutes(5)) // 缓存过期时间（5分钟）
                                                              .build();

    public PictureServiceImpl(UserService userService, FilePictureUpload filePictureUpload, UrlPictureUpload urlPictureUpload, CosManager cosManager, RedissonClient redissonClient, StringRedisTemplate stringRedisTemplate) {
        this.userService = userService;
        this.filePictureUpload = filePictureUpload;
        this.urlPictureUpload = urlPictureUpload;
        this.cosManager = cosManager;
        this.redissonClient = redissonClient;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 上传一张图片
     *
     * @param inputSource 目标文件源
     * @param uploadDTO   上传图片请求封装类
     * @param request     用户请求
     * @return 返回图片信息封装类
     */
    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadDTO uploadDTO, HttpServletRequest request) {
        // 判断是新增还是更新
        Long pictureId = null;
        if (uploadDTO != null) {
            pictureId = uploadDTO.getId();
        }
        // 如果是更新，先查询数据库中有无该图片，因为先得有才能更新
        User currentUser = userService.getCurrentUser(request);
        Picture oldPicture = null;
        if (pictureId != null) {
            // 检查要更新的图片是否存在
            oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(
                    oldPicture == null,
                    ErrorCode.NOT_FOUND_ERROR,
                    "图片不存在！"
            );
            // 检查用户权限
            // 如果不是本人并且也不是管理员就没有权限
            ThrowUtils.throwIf(
                    !oldPicture.getUserId().equals(currentUser.getId()) &&
                            !UserConstant.ROLE_ADMIN.equals(currentUser.getUserRole()),
                    ErrorCode.NO_AUTH_ERROR
            );
        }
        // 上传图片
        String folderPath = String.format("public/%s", currentUser.getId());
        // 编译看左运行看右，动态匹配类型
        PictureUploadTemplate pictureUpload = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUpload = urlPictureUpload;
        }
        UploadPictureResultDTO uploadPictureResultDTO = pictureUpload.uploadPicture(inputSource, folderPath);
        //操作数据库
        Picture picture = new Picture();
        BeanUtils.copyProperties(uploadPictureResultDTO, picture);
        picture.setUserId(currentUser.getId());
        // 支持自定义图片名称
        if (uploadDTO != null && uploadDTO.getFileName() != null) {
            picture.setName(uploadDTO.getFileName());
        }
        if (pictureId != null) {
            picture.setId(pictureId);
        }
        resetReviewStatus(picture, currentUser);
        // 根据id是否有值来判断更新还是插入
        boolean savaResult = this.saveOrUpdate(picture);
        // 如果oldPicture存在则说明是更新，把就图片删除
        if (oldPicture != null) {
            clearPicture(oldPicture);
        }
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
        Integer reviewStatus = pictureQueryDTO.getReviewStatus();
        String reviewMessage = pictureQueryDTO.getReviewMessage();
        Long reviewerId = pictureQueryDTO.getReviewerId();
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
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), Picture::getReviewMessage, reviewMessage);
        queryWrapper.eq(StrUtil.isNotBlank(category), Picture::getCategory, category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), Picture::getPicWidth, picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), Picture::getPicHeight, picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), Picture::getPicSize, picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), Picture::getPicScale, picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), Picture::getReviewStatus, reviewStatus);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), Picture::getReviewerId, reviewerId);
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

    /**
     * 图片审核实现类
     *
     * @param pictureReviewDTO 图片审核信息封装类
     * @param request          用户请求
     */
    @Override
    public void pictureReview(PictureReviewDTO pictureReviewDTO, HttpServletRequest request) {
        //校验参数
        ThrowUtils.throwIf(pictureReviewDTO == null, ErrorCode.PARAMS_ERROR);
        Long id = pictureReviewDTO.getId();
        Integer reviewStatus = pictureReviewDTO.getReviewStatus();
        String reviewMessage = pictureReviewDTO.getReviewMessage();
        ThrowUtils.throwIf(ObjUtil.isNull(id) || id <= 0, ErrorCode.PARAMS_ERROR);
        // 如果没有获取到状态信息则说明审核状态为null或填写错误
        PictureReviewStatusEnum status = PictureReviewStatusEnum.findByValue(reviewStatus);
        // 要么是审核通过，要么是不通过，当状态为待审核时抛出异常
        ThrowUtils.throwIf(
                status == null || PictureReviewStatusEnum.REVIEWING.equals(status),
                ErrorCode.PARAMS_ERROR
        );
        //判断图片是否存在
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(
                oldPicture == null,
                ErrorCode.PARAMS_ERROR,
                "目标图片不存在！"
        );
        // 判断审核状态是否重复
        ThrowUtils.throwIf(
                oldPicture.getReviewStatus().equals(reviewStatus),
                ErrorCode.PARAMS_ERROR,
                "请勿重复审核！"
        );
        // 操作数据库
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureReviewDTO, picture);
        picture.setReviewerId(userService.getCurrentUser(request).getId());
        picture.setReviewTime(LocalDateTime.now());
        boolean save = this.updateById(picture);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR);
    }

    /**
     * 重置审核状态，管理员自动过审
     *
     * @param picture   目标图片
     * @param loginUser 当前用户
     */
    @Override
    public void resetReviewStatus(Picture picture, User loginUser) {
        if (UserConstant.ROLE_ADMIN.equals(loginUser.getUserRole())) {
            // 管理员自动过审
            picture.setReviewerId(loginUser.getId());
            picture.setReviewTime(LocalDateTime.now());
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewMessage("管理员自动过审！");
        } else {
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    /**
     * 批量上传图片
     *
     * @param pictureUploadByBatchDTO 批量上传图片请求封装类
     * @param request                 用户请求
     * @return 返回成功上传图片的数量
     */
    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchDTO pictureUploadByBatchDTO, HttpServletRequest request) {
        // 校验参数
        String searchText = pictureUploadByBatchDTO.getSearchText();
        Integer count = pictureUploadByBatchDTO.getCount();
        String namePrefix = pictureUploadByBatchDTO.getNamePrefix();
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }
        ThrowUtils.throwIf(
                count == null || count <= 0 || count > 30 || StrUtil.isBlank(searchText), ErrorCode.PARAMS_ERROR,
                "搜索参数错误！"
        );
        // 抓取内容
        String fechUrl = String.format("https://cn.bing.com/images/async?q=%25s&mmasync=1", searchText);
        Document elements;
        try {
            elements = Jsoup.connect(fechUrl).get();
        } catch (IOException e) {
            log.error("获取网页内容失败！{}", e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "抓取失败！");
        }
        //解析内容
        Element dgControlDiv = elements.getElementsByClass("dgControl").first();
        ThrowUtils.throwIf(
                dgControlDiv == null,
                ErrorCode.OPERATION_ERROR,
                "获取页面元素失败！"
        );
        Elements imgList = dgControlDiv.select("img.mimg");
        // 设置一个记录图片上传数量的变量
        int uploadCount = 0;
        for (Element img : imgList) {
            // 获取图片链接
            String imgUrl = img.attr("src");
//            log.info("未处理的图片链接：{}", imgUrl);
            if (StrUtil.isBlank(imgUrl)) {
                log.info("链接为空，跳过：{}", fechUrl);
                continue;
            }
            // 处理图片链接，获取纯净的链接
            int questionIndex = imgUrl.indexOf("?");
            if (questionIndex != -1) {
                imgUrl = imgUrl.substring(0, questionIndex);
            }
            // 上传图片
            try {
                PictureUploadDTO pictureUploadDTO = new PictureUploadDTO();
                pictureUploadDTO.setFileName(namePrefix + (uploadCount + 1));
//                log.info("图片链接：{}", imgUrl);
                this.uploadPicture(imgUrl, pictureUploadDTO, request);
                uploadCount++;
            } catch (Exception e) {
                log.error("上传失败：{}", imgUrl);
                continue;
            }
            if (uploadCount == count) {
                break;
            }
        }
        return uploadCount;
    }

    /**
     * 采用异步，删除目标图片
     *
     * @param picture 目标图片
     */
    @Async
    @Override
    public void clearPicture(Picture picture) {
        String url = picture.getUrl();
        String path = URLUtil.getPath(url);
        Long count = this.lambdaQuery().eq(Picture::getUrl, url).count();
        // 如果图片链接大于说明图片链接出现复用，不执行删除
        if (count > 1) {
            return;
        }
        // 执行删除操作
        cosManager.deleteObject(path);
        // 请求该图片所对应的缩略图
        String thumbnailUrl = picture.getThumbnailUrl();
        if (StrUtil.isNotBlank(thumbnailUrl)) {
            String thumbnailPath = URLUtil.getPath(thumbnailUrl);
            cosManager.deleteObject(thumbnailPath);
        }
    }

    /**
     * 从分级缓存中获取分页数据，采用分布式锁来解决缓存穿透
     * @param pictureQueryDTO 分页请求封装类对象
     * @return PictureVO类型的Page封装类
     */
    @Override
    public Page<PictureVO> listPictureVOByPageWithCache(PictureQueryDTO pictureQueryDTO) {
        long current = pictureQueryDTO.getCurrent();
        long size = pictureQueryDTO.getPageSize();
        // 构建Redis的key
        // 将查询条件转换成Json字符串
        String queryStr = JSONUtil.toJsonStr(pictureQueryDTO);
        // 将字符串压缩成md5字符串
        String hashKey = DigestUtils.md5DigestAsHex(queryStr.getBytes());
        // 拼接组成RedisKey
        String cacheKey = String.format("smart-picture:listPictureVOByPage:%s", hashKey);
        // 1. 先从本地缓存获取数据
        String localCache = LOCAL_CACHE.getIfPresent(cacheKey);
        if (localCache != null) {
            Page<PictureVO> bean = JSONUtil.toBean(localCache, Page.class);
            return bean;
        }
        // 2. 如果本地缓存没有再从Redis中获取
        // 获取操作对象
        ValueOperations<String, String> stringStringValueOperations = stringRedisTemplate.opsForValue();
        // 先从Redis中获取，如果有，先刷新本地缓存，然后将结果返回给前端
        String objectFromRedis = stringStringValueOperations.get(cacheKey);
        if (objectFromRedis != null) {
            LOCAL_CACHE.put(cacheKey, objectFromRedis);
            Page<PictureVO> bean = JSONUtil.toBean(objectFromRedis, Page.class);
            return bean;
        }
        // 3. 如果本地缓存和Redis都没有，先从数据库查询，然后再刷新本地缓存和Redis缓存
        // 采用分布式锁来解决缓存穿透
        Page<PictureVO> pictureVOPage = null;
        RLock lock = redissonClient.getLock(cacheKey);
        try {
            // 采用默认看门狗机制来续租
            if (lock.tryLock(3,TimeUnit.SECONDS)) {
                // 设置查询条件，只查询通过审核的
                pictureQueryDTO.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
                // 查询数据库
                Page<Picture> picturePage = this.page(
                        new Page<>(current, size),
                        this.getQueryWrapper(pictureQueryDTO)
                );
                pictureVOPage = this.getPictureVOPage(picturePage);
                // 将查询结果存入Redis，过期时间使用固定随机数，避免缓存雪崩
                int expiryTime = 300 + RandomUtil.randomInt(0, 300);
                // 将结果放入Redis缓存
                stringStringValueOperations.set(cacheKey, JSONUtil.toJsonStr(pictureVOPage), expiryTime, TimeUnit.SECONDS);
                // 将结果放入本地缓存
                LOCAL_CACHE.put(cacheKey, JSONUtil.toJsonStr(pictureVOPage));
            } else {
                // 如果没抢到锁，进行重试
                int tries = 0;
                while (tries++ < 5) {
                    Thread.sleep(2000);
                    // 1. 先从本地缓存获取数据
                    String TryLocalCache = LOCAL_CACHE.getIfPresent(cacheKey);
                    if (TryLocalCache != null) {
                        Page<PictureVO> bean = JSONUtil.toBean(TryLocalCache, Page.class);
                        return bean;
                    }
                    // 2. 如果本地缓存没有再从Redis中获取
                    // 先从Redis中获取，如果有，先刷新本地缓存，然后将结果返回给前端
                    String TryObjectFromRedis = stringStringValueOperations.get(cacheKey);
                    if (TryObjectFromRedis != null) {
                        LOCAL_CACHE.put(cacheKey, TryObjectFromRedis);
                        Page<PictureVO> bean = JSONUtil.toBean(TryObjectFromRedis, Page.class);
                        return bean;
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("抢锁时出现错误：{}", e.getMessage());
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return pictureVOPage;
    }

}





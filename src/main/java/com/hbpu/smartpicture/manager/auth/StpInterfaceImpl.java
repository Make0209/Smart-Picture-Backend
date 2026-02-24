package com.hbpu.smartpicture.manager.auth;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import com.hbpu.smartpicture.exception.BusinessException;
import com.hbpu.smartpicture.exception.ErrorCode;
import com.hbpu.smartpicture.manager.auth.model.SpaceUserPermissionConstant;
import com.hbpu.smartpicture.model.enums.SpaceRoleEnum;
import com.hbpu.smartpicture.model.enums.SpaceTypeEnum;
import com.hbpu.smartpicture.model.pojo.Picture;
import com.hbpu.smartpicture.model.pojo.Space;
import com.hbpu.smartpicture.model.pojo.SpaceUser;
import com.hbpu.smartpicture.model.pojo.User;
import com.hbpu.smartpicture.service.PictureService;
import com.hbpu.smartpicture.service.SpaceService;
import com.hbpu.smartpicture.service.SpaceUserService;
import com.hbpu.smartpicture.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;

/**
 * Sa-Token 自定义权限加载接口实现类
 * <p>
 * 【核心作用】
 * 该类是 Sa-Token 框架的扩展点，负责为"空间"模块提供动态权限查询能力。
 * 当 Controller 方法上标注了 @SaSpaceCheckPermission 注解时，
 * Sa-Token 会自动调用此类的 getPermissionList() 方法来获取当前用户的权限列表，
 * 然后比对用户是否拥有注解要求的权限，从而决定是否放行请求。
 * <p>
 * 【业务场景】
 * 1. 用户访问私有空间：只有空间创建者本人或系统管理员能操作
 * 2. 用户访问团队空间：根据成员角色（admin/editor/viewer）分配不同权限
 * 3. 用户操作公共图库图片：仅图片上传者本人或管理员可操作，其他人只能查看
 * <p>
 * 【调用链】
 * 用户请求 → @SaSpaceCheckPermission 注解拦截 → Sa-Token 框架 → 
 * StpInterfaceImpl.getPermissionList() → 返回权限列表 → Sa-Token 比对权限 → 放行/拒绝
 */
@Component    // 保证此类被 SpringBoot 扫描，完成 Sa-Token 的自定义权限验证扩展
public class StpInterfaceImpl implements StpInterface {
    // ==================== 依赖服务 ====================

    /**
     * 空间用户权限管理器
     * <p>作用：根据角色标识（如"admin"、"editor"）从配置文件中读取该角色拥有的权限列表
     */
    private final SpaceUserAuthManager spaceUserAuthManager;

    /**
     * 空间用户服务
     * <p>作用：查询用户在某个空间中的成员信息（角色、权限等）
     */
    private final SpaceUserService spaceUserService;

    /**
     * 图片服务
     * <p>作用：根据图片ID查询图片信息，用于判断图片所属空间
     */
    private final PictureService pictureService;

    /**
     * 用户服务
     * <p>作用：判断用户是否为系统管理员
     */
    private final UserService userService;

    /**
     * 空间服务
     * <p>作用：根据空间ID查询空间信息，用于判断空间类型（私有/团队）
     */
    private final SpaceService spaceService;

    /**
     * 构造方法注入依赖
     */
    public StpInterfaceImpl(SpaceUserAuthManager spaceUserAuthManager, SpaceUserService spaceUserService,
            PictureService pictureService, UserService userService, SpaceService spaceService) {
        this.spaceUserAuthManager = spaceUserAuthManager;
        this.spaceUserService = spaceUserService;
        this.pictureService = pictureService;
        this.userService = userService;
        this.spaceService = spaceService;
    }


    /**
     * 获取当前登录账号的权限码集合（核心权限判断方法）
     * <p>
     * 【方法说明】
     * 该方法由 Sa-Token 框架自动调用，用于获取指定用户在"空间"模块下的所有权限。
     * 方法会根据请求中的参数（spaceId、pictureId、spaceUserId等）判断用户正在操作什么资源，
     * 然后根据资源类型和用户身份返回对应的权限列表。
     * <p>
     * 【执行流程】
     * 1. 校验 loginType 是否为 "space"，不是则返回空权限
     * 2. 从 HTTP 请求中解析出 spaceId、pictureId、spaceUserId 等参数
     * 3. 根据参数判断业务场景，按优先级处理：
     *    - 场景A：查询公共图库（无参数）→ 返回全部权限
     *    - 场景B：已有 SpaceUser 对象 → 直接返回该角色权限
     *    - 场景C：有 spaceUserId → 查询当前用户在该空间的角色
     *    - 场景D：有 pictureId → 查图片所属空间，再判断空间类型
     *    - 场景E：有 spaceId → 判断空间类型（私有/团队）分别处理
     * 4. 根据空间类型和用户角色返回权限列表
     * <p>
     * 【权限规则】
     * - 私有空间：仅空间创建者本人或系统管理员拥有全部权限
     * - 团队空间：根据 SpaceUser 表中记录的角色（admin/editor/viewer）分配权限
     * - 公共图库图片：仅图片上传者本人或管理员可操作，其他人只能查看
     *
     * @param loginId  登录用户ID（Sa-Token 的登录标识）
     * @param loginType 登录类型，本项目使用 "space" 表示空间模块的权限体系
     * @return 该用户拥有的权限码列表，如 ["picture:view", "picture:upload", "picture:delete"]
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // ========== 步骤1：校验登录类型 ==========
        // Sa-Token 支持多账号体系（如用户端、管理端），本项目只用 "space" 类型做空间权限校验
        // 如果不是 "space" 类型，返回空列表表示无任何权限
        if (!StpKit.SPACE_TYPE.equals(loginType)) {
            return new ArrayList<>();
        }

        // 预定义管理员权限列表（拥有所有权限），后续多处会用到
        List<String> ADMIN_PERMISSIONS = spaceUserAuthManager.getPermissionsByRole(SpaceRoleEnum.ADMIN.getValue());

        // ========== 步骤2：解析请求参数 ==========
        // 从当前 HTTP 请求中解析出 spaceId、pictureId、spaceUserId 等关键参数
        // 这些参数决定了用户在操作什么资源
        SpaceUserAuthContext authContext = getAuthContextByRequest();

        // ========== 步骤3：场景A - 查询公共图库 ==========
        // 如果请求中没有携带任何资源ID（spaceId、pictureId、spaceUserId都为空）
        // 说明用户在查询公共图库列表，这种情况默认放行，返回全部权限
        if (isAllFieldsNull(authContext)) {
            return ADMIN_PERMISSIONS;
        }

        // ========== 步骤4：获取当前登录用户信息 ==========
        // 从 Sa-Token 的 Session 中取出完整的 User 对象，进而获取 userId
        User loginUser = (User) StpKit.SPACE.getSessionByLoginId(loginId).get("user");
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户未登录");
        }
        Long userId = loginUser.getId();

        // ========== 步骤5：场景B - 上下文中已有 SpaceUser 对象 ==========
        // 如果请求处理过程中已经查询出了 SpaceUser 对象（比如在 Service 层已设置）
        // 直接根据该对象的角色返回权限，避免重复查询数据库
        SpaceUser spaceUser = authContext.getSpaceUser();
        if (spaceUser != null) {
            return spaceUserAuthManager.getPermissionsByRole(spaceUser.getSpaceRole());
        }

        // ========== 步骤6：场景C - 通过 spaceUserId 查询 ==========
        // 如果请求中携带了 spaceUserId（例如：编辑某个空间成员信息时）
        // 先查出该 spaceUserId 对应的空间成员，再查询当前登录用户在该空间的角色
        Long spaceUserId = authContext.getSpaceUserId();
        if (spaceUserId != null) {
            // 根据 spaceUserId 查询目标空间用户信息
            spaceUser = spaceUserService.getById(spaceUserId);
            if (spaceUser == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到空间用户信息");
            }
            // 查询当前登录用户在【该空间】中的成员信息（注意：不是查 spaceUserId 对应的用户）
            SpaceUser loginSpaceUser = spaceUserService.lambdaQuery()
                                                       .eq(SpaceUser::getSpaceId, spaceUser.getSpaceId())
                                                       .eq(SpaceUser::getUserId, userId)
                                                       .one();
            // 如果当前用户不是该空间的成员，返回空权限（无任何操作权限）
            if (loginSpaceUser == null) {
                return new ArrayList<>();
            }
            // 根据当前用户在该空间的角色返回权限
            // 【注意】这里有个潜在问题：如果是私有空间的管理员，可能查不到 SpaceUser 记录
            return spaceUserAuthManager.getPermissionsByRole(loginSpaceUser.getSpaceRole());
        }

        // ========== 步骤7：场景D/E - 通过 spaceId 或 pictureId 查询 ==========
        // 先尝试获取 spaceId，如果没有则通过 pictureId 间接获取
        Long spaceId = authContext.getSpaceId();

        // 如果没有直接提供 spaceId，但提供了 pictureId，则查询图片信息获取 spaceId
        if (spaceId == null) {
            Long pictureId = authContext.getPictureId();
            // 如果连 pictureId 也没有，说明是未知场景，默认放行（返回全部权限）
            if (pictureId == null) {
                return ADMIN_PERMISSIONS;
            }

            // 查询图片信息，只获取必要的字段（id、spaceId、userId）
            Picture picture = pictureService.lambdaQuery()
                                            .eq(Picture::getId, pictureId)
                                            .select(Picture::getId, Picture::getSpaceId, Picture::getUserId)
                                            .one();
            if (picture == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到图片信息");
            }
            spaceId = picture.getSpaceId();

            // ========== 子场景：公共图库的图片（spaceId 为 null） ==========
            // 如果图片的 spaceId 为 null，说明该图片属于公共图库（不在任何空间里）
            // 公共图库的图片权限规则：仅图片上传者本人或系统管理员可操作，其他人只能查看
            if (spaceId == null) {
                // 如果是图片上传者本人，或者是系统管理员，返回全部权限
                if (picture.getUserId().equals(userId) || userService.isAdmin(userId)) {
                    return ADMIN_PERMISSIONS;
                } else {
                    // 其他人只能查看，不能编辑、删除
                    return Collections.singletonList(SpaceUserPermissionConstant.PICTURE_VIEW);
                }
            }
        }

        // ========== 步骤8：获取 Space 对象并判断空间类型 ==========
        // 到这里已经确定了 spaceId，查询空间的详细信息
        Space space = spaceService.getById(spaceId);
        if (space == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到空间信息");
        }

        // ========== 步骤9：根据空间类型分别处理权限 ==========
        if (space.getSpaceType() == SpaceTypeEnum.PRIVATE.getValue()) {
            // ========== 场景：私有空间 ==========
            // 私有空间的权限规则：仅空间创建者本人或系统管理员拥有全部权限，其他人无任何权限
            if (space.getUserId().equals(userId) || userService.isAdmin(userId)) {
                return ADMIN_PERMISSIONS;
            } else {
                // 不是空间创建者，也不是管理员，返回空权限
                return new ArrayList<>();
            }
        } else {
            // ========== 场景：团队空间 ==========
            // 团队空间的权限规则：查询 SpaceUser 表，根据用户的角色分配权限
            // 先查询当前用户是否是该团队的成员
            spaceUser = spaceUserService.lambdaQuery()
                                        .eq(SpaceUser::getSpaceId, spaceId)
                                        .eq(SpaceUser::getUserId, userId)
                                        .one();
            // 如果不是该团队空间的成员，返回空权限
            if (spaceUser == null) {
                return new ArrayList<>();
            }
            // 是团队成员，根据角色返回对应的权限列表
            return spaceUserAuthManager.getPermissionsByRole(spaceUser.getSpaceRole());
        }
    }


    /**
     * 返回当前登录账号的角色标识集合
     * <p>
     * 【方法说明】
     * 该方法也是 Sa-Token 框架的接口方法，用于获取用户的角色列表。
     * 与 getPermissionList() 不同，这里返回的是角色（如 "admin"、"editor"），
     * 而 getPermissionList() 返回的是具体的权限（如 "picture:delete"）。
     * <p>
     * 【当前实现】
     * 本项目主要使用权限码（permission）进行细粒度控制，
     * 角色（role）仅作为内部标识，不在 Sa-Token 层面做角色校验，
     * 因此该方法返回空列表。
     * <p>
     * 【使用场景】
     * 如果未来需要基于角色做粗粒度控制（如 @SaCheckRole("admin")），
     * 可以在此方法中实现角色查询逻辑。
     *
     * @param loginId   登录用户ID
     * @param loginType 登录类型
     * @return 角色标识列表，当前返回空列表
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 本项目不使用 Sa-Token 的角色校验功能，直接返回空列表
        return new ArrayList<>();
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 服务器上下文路径，用于截取请求 URI 中的模块名
     * <p>例如：contextPath="/api"，请求 URI="/api/picture/delete"，截取后得到 "picture"
     */
    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * 从当前 HTTP 请求中解析出权限校验所需的上下文参数
     * <p>
     * 【方法说明】
     * 该方法负责读取 HTTP 请求中的参数，构建 SpaceUserAuthContext 对象。
     * 它会兼容处理 GET 请求（URL参数）和 POST 请求（JSON体），
     * 并根据请求路径智能判断 "id" 字段的具体含义。
     * <p>
     * 【参数解析规则】
     * 1. 读取请求参数：支持 URL 参数（?id=100）和 JSON 请求体（{"id":100}）
     * 2. 智能识别 id 含义：
     *    - 请求路径是 /picture/xxx  →  id 是 pictureId（图片ID）
     *    - 请求路径是 /spaceUser/xxx →  id 是 spaceUserId（空间用户ID）
     *    - 请求路径是 /space/xxx    →  id 是 spaceId（空间ID）
     * <p>
     * 【为什么需要智能识别？】
     * 前端传参时通常只传一个 "id" 字段，但后端需要知道这是什么资源的ID。
     * 通过请求路径的模块名，可以准确判断 id 的类型。
     * <p>
     * 【示例】
     * - 请求 POST /api/picture/delete，body: {"id": 100}
     * - 解析结果：SpaceUserAuthContext.pictureId = 100
     *
     * @return 包含解析后参数的上下文对象
     */
    private SpaceUserAuthContext getAuthContextByRequest() {
        // 从 Spring 的请求上下文持有者中获取当前 HTTP 请求对象
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        // 获取请求的 Content-Type，用于判断是 JSON 请求还是表单请求
        String contentType = request.getHeader(Header.CONTENT_TYPE.getValue());
        SpaceUserAuthContext authRequest;

        // ========== 步骤1：根据 Content-Type 读取请求参数 ==========
        // 兼容两种传参方式：
        // 1. POST + JSON：body 为 {"id": 100, "spaceId": 10}
        // 2. GET/POST + 表单：URL 参数或 form-data，如 ?id=100&spaceId=10
        if (ContentType.JSON.getValue().equals(contentType)) {
            // JSON 请求：读取请求体字符串，然后转换为对象
            String body = readRequestBody(request);
            authRequest = JSONUtil.toBean(body, SpaceUserAuthContext.class);
        } else {
            // 表单请求：从 request.getParameterMap() 中读取参数
            Map<String, String> paramMap = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                // 如果参数有多个值，只取第一个值
                if (values != null && values.length > 0) {
                    paramMap.put(key, values[0]);
                }
            });
            // 将 Map 转换为 SpaceUserAuthContext 对象
            authRequest = BeanUtil.toBean(paramMap, SpaceUserAuthContext.class);
        }

        // ========== 步骤2：智能识别 id 字段的含义 ==========
        // 如果请求中携带了 id 字段，根据请求路径判断它代表什么资源的ID
        Long id = authRequest.getId();
        if (ObjUtil.isNotNull(id)) {
            // 获取完整的请求 URI，例如："/api/picture/delete"
            String requestUri = request.getRequestURI();
            // 去掉上下文路径，得到 "picture/delete"
            String partUri = requestUri.replace(contextPath + "/", "");
            // 截取第一个 / 之前的部分，得到模块名 "picture"
            String moduleName = StrUtil.subBefore(partUri, "/", false);

            // 根据模块名，将 id 设置到对应的字段中
            switch (moduleName) {
                case "picture":
                    // 图片模块：id 是 pictureId
                    authRequest.setPictureId(id);
                    break;
                case "spaceUser":
                    // 空间用户模块：id 是 spaceUserId
                    authRequest.setSpaceUserId(id);
                    break;
                case "space":
                    // 空间模块：id 是 spaceId
                    authRequest.setSpaceId(id);
                    break;
                default:
                    // 其他模块：不做处理，保持 id 原值
                    break;
            }
        }
        return authRequest;
    }

    /**
     * 读取 HTTP 请求体内容（兼容 jakarta.servlet）
     * <p>
     * 【方法说明】
     * 该方法用于读取 POST 请求的 JSON 请求体。
     * 原本可以使用 Hutool 的 ServletUtil.getBody()，但那个方法基于 javax.servlet 包，
     * 而本项目使用 Spring Boot 3 + jakarta.servlet，因此需要自己实现。
     * <p>
     * 【使用场景】
     * 当 Content-Type 为 application/json 时，调用此方法读取请求体字符串，
     * 然后使用 JSONUtil.toBean() 转换为 Java 对象。
     *
     * @param request HTTP 请求对象
     * @return 请求体字符串（JSON 格式）
     * @throws BusinessException 如果读取失败，抛出参数错误异常
     */
    private String readRequestBody(HttpServletRequest request) {
        StringBuilder body = new StringBuilder();
        // 使用 try-with-resources 自动关闭 BufferedReader
        try (java.io.BufferedReader reader = request.getReader()) {
            String line;
            // 逐行读取请求体内容
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        } catch (java.io.IOException e) {
            // 读取失败时抛出业务异常，由全局异常处理器返回错误信息
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "读取请求体失败");
        }
        return body.toString();
    }

    /**
     * 检查对象的所有字段是否都为空
     * <p>
     * 【方法说明】
     * 该方法用于判断 SpaceUserAuthContext 对象是否没有任何有效参数。
     * 如果所有字段（id、spaceId、pictureId、spaceUserId 等）都为 null，
     * 说明用户请求中没有携带任何资源标识，通常是在查询公共图库列表。
     * <p>
     * 【实现原理】
     * 使用 Hutool 的 ReflectUtil 反射获取类的所有字段，
     * 然后遍历每个字段，使用 ReflectUtil 获取字段值，
     * 最后用 ObjectUtil.isEmpty() 判断是否所有字段都为空。
     * <p>
     * 【使用场景】
     * 在 getPermissionList() 方法中，如果此方法返回 true，
     * 表示查询公共图库，直接返回全部权限，无需进一步判断。
     *
     * @param object 要检查的对象
     * @return true-所有字段都为空或对象本身为null；false-至少有一个字段不为空
     */
    private boolean isAllFieldsNull(Object object) {
        // 如果对象本身为 null，认为所有字段都为空
        if (object == null) {
            return true;
        }
        // 使用反射获取类的所有字段，并检查每个字段的值
        return Arrays.stream(ReflectUtil.getFields(object.getClass()))
                     // 获取每个字段的值
                     .map(field -> ReflectUtil.getFieldValue(object, field))
                     // 检查是否所有字段都为空（使用 ObjectUtil.isEmpty 判断）
                     .allMatch(ObjectUtil::isEmpty);
    }

}

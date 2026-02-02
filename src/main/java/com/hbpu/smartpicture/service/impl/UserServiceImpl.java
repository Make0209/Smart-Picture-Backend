package com.hbpu.smartpicture.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbpu.smartpicture.exception.BusinessException;
import com.hbpu.smartpicture.exception.ErrorCode;
import com.hbpu.smartpicture.exception.ThrowUtils;
import com.hbpu.smartpicture.mapper.UserMapper;
import com.hbpu.smartpicture.model.dto.user.UserQueryDTO;
import com.hbpu.smartpicture.model.dto.user.UserRegisterDTO;
import com.hbpu.smartpicture.model.enums.UserRoleEnum;
import com.hbpu.smartpicture.model.pojo.User;
import com.hbpu.smartpicture.model.vo.user.UserLoginVO;
import com.hbpu.smartpicture.model.vo.user.UserVO;
import com.hbpu.smartpicture.security.JwtUtil;
import com.hbpu.smartpicture.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author 马可
 * &#064;description  针对表【user(用户)】的数据库操作Service实现
 * &#064;createDate  2025-11-10 17:42:36
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    private final RedissonClient redisson;

    public UserServiceImpl(RedissonClient redisson) {
        this.redisson = redisson;
    }

    /**
     * 用户注册方法具体实现
     *
     * @param userRegisterDTO 用户注册请求封装对象
     * @return 返回注册成功后的用户id
     */
    @Override
    public Long userRegister(UserRegisterDTO userRegisterDTO) {
        //先校验参数
        //判断参数中是否有空字符串，如果有直接抛出业务异常
        ThrowUtils.throwIf(
                StrUtil.hasBlank(
                        userRegisterDTO.getUserAccount(),
                        userRegisterDTO.getUserPassword(),
                        userRegisterDTO.getCheckPassword()
                ),
                new BusinessException(ErrorCode.PARAMS_ERROR)
        );
        //判断用户账号是否过短
        ThrowUtils.throwIf(
                userRegisterDTO.getUserAccount().length() < 4,
                new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短")
        );
        //判断用户的密码格式是否正确
        ThrowUtils.throwIf(
                userRegisterDTO.getUserPassword().length() < 8 || userRegisterDTO.getCheckPassword().length() < 8,
                new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过短！")
        );
        //判断用户两次密码是否一致
        ThrowUtils.throwIf(
                !userRegisterDTO.getUserPassword().equals(userRegisterDTO.getCheckPassword()),
                new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致！")
        );
        //检查用户账号是否重复
        Long result = this.baseMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(
                        User::getUserAccount,
                        userRegisterDTO.getUserAccount()
                )
        );
        //从数据库查询到的数据大于1就说明有重复
        ThrowUtils.throwIf(result > 0, new BusinessException(ErrorCode.PARAMS_ERROR, "账户已经被注册！"));
        //将密码加密
        User user = new User();
        user.setUserAccount(userRegisterDTO.getUserAccount());
        user.setUserPassword(getEncryptedPassword(userRegisterDTO.getUserPassword()));
        user.setUserName("用户：" + userRegisterDTO.getUserAccount());
        user.setUserRole(UserRoleEnum.USER.getValue());
        //将数据插入数据库中
        ThrowUtils.throwIf(!this.save(user), new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败！数据库插入失败！"));
        //主键回填给user对象
        return user.getId();
    }

    /**
     * 用户登录方法实现类
     *
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @return 用户登录请求响应封装类
     */
    @Override
    public UserLoginVO userLogin(String userAccount, String userPassword) {
        //验证参数是否为空
        ThrowUtils.throwIf(
                StrUtil.hasBlank(userAccount, userPassword),
                new BusinessException(ErrorCode.PARAMS_ERROR)
        );
        //判断用户账号是否过短
        ThrowUtils.throwIf(
                userAccount.length() < 4,
                new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短")
        );
        //判断用户的密码格式是否正确
        ThrowUtils.throwIf(
                userPassword.length() < 8,
                new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过短！")
        );
        //获取加密后的密码
        String encryptedPassword = getEncryptedPassword(userPassword);
        //从数据库中查询用户
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getUserAccount, userAccount)
                              .eq(User::getUserPassword, encryptedPassword);
        User user = this.baseMapper.selectOne(userLambdaQueryWrapper);
        if (user == null) {
            log.error("User not found !");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号或密码错误！");
        }
        //将用户数据映射为请求包装类
        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtils.copyProperties(user, userLoginVO);
        //为当前用户生成唯一token
        String token = JwtUtil.generateJwt(userAccount);
        //将token放入响应体中
        userLoginVO.setToken(token);
        // 使用用户id拼接来组成独立的每个key
        String redisKey = String.format("smart-picture:user:login:token:%s", userLoginVO.getUserAccount());
        // 创建一个Map
        RMapCache<Object, Object> mapCache = redisson.getMapCache(redisKey);
        // 设置其值
        mapCache.put("token", token, 30, TimeUnit.MINUTES);
        mapCache.put("object", user, 30, TimeUnit.MINUTES);

        return userLoginVO;
    }

    /**
     * 查询当前用户
     *
     * @param request 用户请求
     * @return 用户对象
     */
    @Override
    public User getCurrentUser(HttpServletRequest request) {
        // 获取请求头中的Authorization，因为它一般存储着token
        String authHeader = request.getHeader("Authorization");
        // 判断是否为空，且其中内容是否正确
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // 去掉 "Bearer " 前缀
            String token = authHeader.substring(7);
            // 解析token
            Claims claims = JwtUtil.parseToken(token);
            // 使用token中负载的主题来拼接redis的key
            String redisKey = String.format("smart-picture:user:login:token:%s", claims.getSubject());
            // 通过key来获取相应的map和其中存储的对象
            User user = (User) redisson.getMapCache(redisKey).get("object");
            // 判断所获取的对象是否存在
            ThrowUtils.throwIf(user == null, new BusinessException(ErrorCode.NOT_LOGIN_ERROR));
            // 存在则返回处理后的对象
            return user;
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "获取token时出现异常");
        }
    }

    /**
     * 用户注销
     *
     * @param request 用户请求
     * @return 注销成功返回true
     */
    @Override
    public Boolean loginOut(HttpServletRequest request) {
        // 获取请求头中的Authorization，因为它一般存储着token
        String authHeader = request.getHeader("Authorization");
        // 判断是否为空，且其中内容是否正确
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // 去掉 "Bearer " 前缀
            String token = authHeader.substring(7);
            // 解析token
            Claims claims = JwtUtil.parseToken(token);
            // 使用token中负载的主题来拼接redis的key
            String redisKey = String.format("smart-picture:user:login:token:%s", claims.getSubject());
            // 通过key来获取相应的map并进行删除
            boolean delete = redisson.getMapCache(redisKey).delete();
            if (!delete) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注销失败！");
            }
            return true;
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "获取token时出现错误！");
        }

    }

    /**
     * 获取单个脱敏后的UserVO对象
     *
     * @param user 用户实体类
     * @return UserVO对象
     */
    @Override
    public UserVO getUserVO(User user) {
        ThrowUtils.throwIf(user == null, new BusinessException(ErrorCode.PARAMS_ERROR));
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 获取脱敏后的UserVO对象列表
     *
     * @param users 用户实体类
     * @return UserVO对象列表
     */
    @Override
    public List<UserVO> getUserVOList(List<User> users) {
        ThrowUtils.throwIf(users == null, new BusinessException(ErrorCode.PARAMS_ERROR));
        return users.stream().map(this::getUserVO).toList();
    }

    /**
     * 根据条件生成查询条件
     *
     * @param userQueryDTO 用户分页查询请求封装类
     * @return 返回一个构建完成的查询条件对象
     */
    @Override
    public LambdaQueryWrapper<User> getQueryWrapper(UserQueryDTO userQueryDTO) {
        ThrowUtils.throwIf(userQueryDTO == null, new BusinessException(ErrorCode.PARAMS_ERROR));
        //获取其中所有的属性值
        Long id = userQueryDTO.getId();
        String userName = userQueryDTO.getUserName();
        String userAccount = userQueryDTO.getUserAccount();
        String userProfile = userQueryDTO.getUserProfile();
        String userRole = userQueryDTO.getUserRole();
        String sortField = userQueryDTO.getSortField();
        String sortOrder = userQueryDTO.getSortOrder();
        //获取查询条件对象
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        //验证属性值并根据其构建查询语句
        wrapper.eq(ObjUtil.isNotNull(id), User::getId, id)
               .like(StrUtil.isNotBlank(userAccount), User::getUserAccount, userAccount)
               .like(StrUtil.isNotBlank(userName), User::getUserName, userName)
               .like(StrUtil.isNotBlank(userProfile), User::getUserProfile, userProfile)
               .eq(StrUtil.isNotBlank(userRole), User::getUserRole, userRole);
        //拼接字符串
        if (StrUtil.isNotBlank(sortField)) {
            String orderSql = "ORDER BY " + sortField + " " + ("ascend".equals(sortOrder) ? "ASC" : "DESC");
            wrapper.last(orderSql);
        }
        return wrapper;
    }


    /**
     * 获取加密后的密码
     *
     * @param password 要加密的密码
     * @return 返回加密后的密码
     */
    @Override
    public String getEncryptedPassword(String password) {
        final String SALT = "Kefan";
        return DigestUtils.md5DigestAsHex((SALT + password).getBytes());
    }

    /**
     * 判断是否为管理员
     *
     * @param request 用户请求
     * @return 是否为管理员
     */
    @Override
    public Boolean isAdmin(HttpServletRequest request) {
        User user = getCurrentUser(request);
        return user.getUserRole().equals("admin");
    }


}





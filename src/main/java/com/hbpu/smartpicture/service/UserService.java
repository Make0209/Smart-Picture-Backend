package com.hbpu.smartpicture.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hbpu.smartpicture.model.dto.user.UserQueryDTO;
import com.hbpu.smartpicture.model.dto.user.UserRegisterDTO;
import com.hbpu.smartpicture.model.pojo.User;
import com.hbpu.smartpicture.model.vo.user.UserLoginVO;
import com.hbpu.smartpicture.model.vo.user.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * @author 马可
 * &#064;description  针对表【user(用户)】的数据库操作Service
 * &#064;createDate  2025-11-10 17:42:36
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册方法
     *
     * @param userRegisterDTO 用户注册请求封装对象
     * @return 用户注册成功后的id
     */
    Long userRegister(UserRegisterDTO userRegisterDTO);

    /**
     * 用户登录方法
     *
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @return 用户登录响应封装类
     */
    UserLoginVO userLogin(String userAccount, String userPassword);

    /**
     * 获取当前登录用户
     *
     * @param request 用户请求
     * @return 当前登录用户封装类
     */
    User getCurrentUser(HttpServletRequest request);

    /**
     * 用户注销方法
     *
     * @param request 用户请求
     * @return 注销成功返回true
     */
    Boolean loginOut(HttpServletRequest request);

    /**
     * 获取脱敏后的用户信息
     *
     * @param user 用户实体类
     * @return 返回脱敏后的UserVO对象
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏后的用户信息列表
     *
     * @param users 用户实体类
     * @return 返回脱敏后的UserVO对象列表
     */
    List<UserVO> getUserVOList(List<User> users);

    /**
     * 根据传入的DTO对象来生成查询条件
     *
     * @param userQueryDTO 用户分页查询请求封装类
     * @return 返回一个查询条件对象
     */
    LambdaQueryWrapper<User> getQueryWrapper(UserQueryDTO userQueryDTO);

    /**
     * 获取加密后的密码
     * @param password 要加密的密码
     * @return 返回加密后的密码
     */
    String getEncryptedPassword(String password);

    /**
     * 判断是否为管理员
     *
     * @param request 用户请求
     * @return 是否为管理员
     */
    Boolean isAdmin(HttpServletRequest request);

    /**
     * 判断是否为管理员
     *
     * @param userId 用户id
     * @return 是否为管理员
     */
    Boolean isAdmin(Long userId);
}
